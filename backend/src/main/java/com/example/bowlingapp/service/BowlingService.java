package com.example.bowlingapp.service;

import com.example.bowlingapp.model.Frame;
import com.example.bowlingapp.model.Game;
import com.example.bowlingapp.repository.FrameRepository;
import com.example.bowlingapp.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * ボウリングゲームのスコア管理を行うサービスクラス。
 * フレームごとの投球記録、スコア計算、およびゲームの状態管理を担当する。
 */
@Service
public class BowlingService {
    private final GameRepository gameRepository;
    private final FrameRepository frameRepository;

    /**
     * コンストラクタ
     * @param gameRepository ゲーム情報を永続化するリポジトリ
     * @param frameRepository フレーム情報を永続化するリポジトリ
     */
    public BowlingService(GameRepository gameRepository, FrameRepository frameRepository) {
        this.gameRepository = gameRepository;
        this.frameRepository = frameRepository;
    }

    /**
     * 新しいボウリングゲームを作成する。
     * 10フレームを初期化し、新しいゲームとして保存する。
     *
     * @return 作成された新しいゲーム
     */
    @Transactional
    public Game createNewGame() {
        Game game = new Game();
        game = gameRepository.save(game);
        initializeFrames(game);
        return game;
    }

    /**
     * ゲームの10フレームを初期化する。
     * 既存のフレームがある場合は削除し、新しいフレームを作成する。
     * 
     * @param game 初期化対象のゲーム
     * @return 初期化された10フレームのリスト
     */
    @Transactional
    private List<Frame> initializeFrames(Game game) {
        // 既存のフレームを削除
        frameRepository.deleteAll(
            frameRepository.findByGameIdOrderByFrameNumber(game.getId())
        );
        
        List<Frame> frames = new ArrayList<>();
        // 10フレームを一度に初期化
        for (int i = 1; i <= 10; i++) {
            Frame frame = new Frame();
            frame.setFrameNumber(i);
            frame.setGame(game);
            frames.add(frame);
        }
        // 一括保存して永続化を確実に
        frames = frameRepository.saveAll(frames);
        frameRepository.flush(); // 即座に永続化
        return frames;
    }

    /**
     * ボウリングの1投球を記録する。
     * 指定されたフレームに投球を記録し、スコアを計算して更新する。
     * 前のフレームが未完了の場合は、そのフレームの2投目として記録する。
     *
     * @param gameId ゲームID
     * @param frameNumber フレーム番号（1-10）
     * @param pins 倒したピンの数（0-10）
     * @return 更新されたゲーム情報
     * @throws IllegalArgumentException pins が 0-10 の範囲外の場合、または不正なフレーム番号の場合
     * @throws IllegalStateException 以下の場合に発生:
     *         - ゲームが既に完了している
     *         - 前のフレームが未完了
     *         - 現在のフレームが既に完了
     *         - 1フレームで合計10ピンを超える投球
     */
    @Transactional
    public Game recordRoll(Long gameId, int frameNumber, int pins) {
        // 入力値のバリデーション
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Invalid pins count");
        }
        if (frameNumber < 1 || frameNumber > 10) {
            throw new IllegalArgumentException("Invalid frame number. Must be between 1 and 10");
        }

        // ゲームの取得と検証
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if (game.isCompleted()) {
            throw new IllegalStateException("Game is already completed");
        }

        // フレームの取得または初期化
        List<Frame> frames = frameRepository.findByGameIdOrderByFrameNumber(gameId);
        if (frames.isEmpty()) {
            frames = initializeFrames(game);
            frameRepository.flush();
        }

        // 現在のフレームを取得
        Frame frameToUpdate = frames.get(frameNumber - 1);

        // 投球対象のフレームを特定
        Frame targetFrame;
        if (frameNumber > 1) {
            Frame previousFrame = frames.get(frameNumber - 2);
            if (!previousFrame.isComplete() && !previousFrame.isStrike() && previousFrame.getSecondRoll() == null) {
                // 前のフレームの2投目として記録
                targetFrame = previousFrame;
                recordRegularFrameRoll(targetFrame, pins);
            } else if (!previousFrame.isComplete()) {
                throw new IllegalStateException("Please complete frame " + (frameNumber - 1) + " first");
            } else {
                // 通常通り現在のフレームに記録
                targetFrame = frameToUpdate;
                if (frameNumber == 10) {
                    recordTenthFrameRoll(targetFrame, pins);
                } else {
                    recordRegularFrameRoll(targetFrame, pins);
                }
            }
        } else {
            // 1フレーム目は直接記録
            targetFrame = frameToUpdate;
            recordRegularFrameRoll(targetFrame, pins);
        }

        // フレームを永続化
        frameRepository.save(targetFrame);
        frameRepository.flush();

        // 最新の状態でスコアを再計算
        frames = frameRepository.findByGameIdOrderByFrameNumber(gameId);
        calculateScores(frames);

        // 一括で永続化
        frames = frameRepository.saveAll(frames);

        // ゲーム状態を更新
        Frame lastFrame = frames.get(9);
        Integer finalScore = lastFrame.getFrameScore();
        game.setTotalScore(finalScore != null ? finalScore : 0);
        game.setCompleted(lastFrame.isComplete());
        game = gameRepository.save(game);

        return game;
    }

    /**
     * 10フレーム目の投球を記録する。
     * 10フレーム目は特別なルールがあり、以下の条件で3投目が許可される：
     * 1. 1投目がストライク
     * 2. 1投目と2投目でスペア
     *
     * @param frame 10フレーム目のフレーム
     * @param pins 倒したピンの数（0-10）
     * @throws IllegalArgumentException 以下の場合に発生:
     *         - 不正なピン数
     *         - 通常の投球で合計が10ピンを超える場合
     * @throws IllegalStateException 以下の場合に発生:
     *         - フレームが既に完了している
     *         - ストライクやスペア以外での3投目の試行
     */
    private void recordTenthFrameRoll(Frame frame, int pins) {
        // 1投目の処理
        if (frame.getFirstRoll() == null) {
            validatePinCount(pins);
            frame.setFirstRoll(pins);
            return;
        }

        // 2投目の処理
        if (frame.getSecondRoll() == null) {
            validatePinCount(pins);
            // ストライクの後は制限なし、通常の場合は合計10ピンまで
            if (!frame.isStrike() && frame.getFirstRoll() + pins > 10) {
                throw new IllegalArgumentException("Total pins cannot exceed 10 in a regular frame");
            }
            frame.setSecondRoll(pins);
            return;
        }

        // 3投目の処理
        if (frame.getThirdRoll() == null) {
            validatePinCount(pins);
            // 3投目が許可されるのは以下の場合：
            // 1. 1投目がストライク
            // 2. 1投目と2投目でスペア
            if (!frame.isStrike() && !frame.isSpare()) {
                throw new IllegalStateException("Third roll is only allowed after strike or spare");
            }
            
            // 2投目がストライクの場合は3投目に制限なし、それ以外は2-3投目で10ピンまで
            Integer secondRoll = frame.getSecondRoll();
            if (secondRoll != null && secondRoll != 10 && secondRoll + pins > 10) {
                throw new IllegalArgumentException("Last two rolls cannot exceed 10 pins total");
            }
            frame.setThirdRoll(pins);
            return;
        }

        throw new IllegalStateException("No more rolls allowed in this frame");
    }

    /**
     * 投球で倒したピン数が有効な範囲内（0-10）かを検証する。
     *
     * @param pins 検証対象のピン数
     * @throws IllegalArgumentException ピン数が0未満または10を超える場合
     */
    private void validatePinCount(int pins) {
        if (pins < 0 || pins > 10) {
            throw new IllegalArgumentException("Pin count must be between 0 and 10");
        }
    }

    /**
     * 通常フレーム（1-9フレーム）の投球を記録する。
     * 1投目でストライクの場合は2投目の記録は行わない。
     * 通常の投球では2投の合計が10ピンを超えることはできない。
     *
     * @param frame 記録対象のフレーム
     * @param pins 倒したピンの数（0-10）
     * @throws IllegalArgumentException 以下の場合に発生:
     *         - 1投目で10ピンを超える
     *         - 2投の合計が10ピンを超える
     * @throws IllegalStateException 以下の場合に発生:
     *         - ストライク後の2投目を試みた場合
     *         - フレームが既に完了している場合
     */
    private void recordRegularFrameRoll(Frame frame, int pins) {
        if (frame.getFirstRoll() == null) {
            if (pins > 10) {
                throw new IllegalArgumentException("First roll cannot exceed 10 pins");
            }
            frame.setFirstRoll(pins);
        } else if (!frame.isStrike()) {
            // ストライクでない場合のみ2投目を記録
            if (frame.getSecondRoll() != null) {
                throw new IllegalStateException("This frame is already complete");
            }
            if (frame.getFirstRoll() + pins > 10) {
                throw new IllegalArgumentException("Total pins cannot exceed 10 in a regular frame");
            }
            frame.setSecondRoll(pins);
        } else {
            throw new IllegalStateException("This frame is already complete with a strike");
        }
    }

    /**
     * 全フレームのスコアを計算する。
     * 以下の特殊ケースを考慮してスコアを計算：
     * - ストライク：次の2投を加算
     * - スペア：次の1投を加算
     * - 10フレーム目：最大3投まで可能
     * 
     * 未完了フレームの場合は暫定スコアを表示：
     * - 通常フレーム：現在の投球のみを集計
     * - ストライク/スペア：次の投球が完了するまで確定しない
     *
     * @param frames 計算対象の全フレームのリスト（10フレーム）
     */
    private void calculateScores(List<Frame> frames) {
        int runningScore = 0;
        
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            
            // フレームが未開始の場合
            if (frame.getFirstRoll() == null) {
                frame.setFrameScore(runningScore);
                continue;
            }

            // 通常の（完了済みの）フレームスコアを計算
            int frameScore = calculateFrameScore(frame, i, frames);

            // スコアの更新と反映
            if (frameScore > 0) {
                // フレームが完了している場合は確定スコアを加算
                runningScore += frameScore;
                frame.setFrameScore(runningScore);
            } else {
                // 未完了フレームの暫定スコアを設定
                if (frame.getFirstRoll() != null) {
                    if (!frame.isStrike() && !frame.isSpare()) {
                        if (frame.getSecondRoll() != null) {
                            // オープンフレームの2投完了
                            int score = frame.getFirstRoll() + frame.getSecondRoll();
                            runningScore += score;
                            frame.setFrameScore(runningScore);
                        } else {
                            // 1投目のみ完了
                            frame.setFrameScore(runningScore + frame.getFirstRoll());
                        }
                    } else {
                        // ストライクまたはスペアで、次のフレームの投球待ち
                        frame.setFrameScore(runningScore);
                    }
                } else {
                    frame.setFrameScore(runningScore);
                }
            }
        }
    }

    /**
     * 個別のフレームのスコアを計算する。
     * フレームの状態（通常、ストライク、スペア）と位置（通常、最終フレーム）に応じて
     * 適切なスコア計算ルールを適用する。
     *
     * @param frame 計算対象のフレーム
     * @param frameIndex フレームの位置（0-9）
     * @param frames 全フレームのリスト
     * @return 計算されたフレームのスコア。フレームが未完了の場合は0
     *         - オープンフレーム：2投の合計
     *         - ストライク：10 + 次の2投の合計
     *         - スペア：10 + 次の1投
     *         - 10フレーム目：最大3投までの合計
     */
    private int calculateFrameScore(Frame frame, int frameIndex, List<Frame> frames) {
        // オープンフレームの場合（2投完了していて、ストライクでもスペアでもない）
        if (frame.getSecondRoll() != null && !frame.isStrike() && !frame.isSpare()) {
            return frame.getFirstRoll() + frame.getSecondRoll();
        }

        // 最終フレームの場合
        if (frameIndex == 9) {
            if (frame.isComplete()) {
                int score = frame.getFirstRoll();
                if (frame.getSecondRoll() != null) {
                    score += frame.getSecondRoll();
                }
                if (frame.getThirdRoll() != null) {
                    score += frame.getThirdRoll();
                }
                return score;
            }
            return 0;
        }

        // 通常フレーム（1-9フレーム）
        Frame nextFrame = frameIndex < 9 ? frames.get(frameIndex + 1) : null;
        
        if (frame.isStrike()) {
            if (nextFrame == null || nextFrame.getFirstRoll() == null) {
                return 0;  // 次のフレームがまだない場合は0を返す
            }
            
            if (nextFrame.isStrike()) {
                Frame nextNextFrame = frameIndex < 8 ? frames.get(frameIndex + 2) : null;
                if (nextNextFrame == null || nextNextFrame.getFirstRoll() == null) {
                    return 0;  // 次の次のフレームがまだない場合は0を返す
                }
                return 20 + nextNextFrame.getFirstRoll();  // ストライク + ストライク + 次の1投目
            } else {
                if (nextFrame.getSecondRoll() == null) {
                    return 0;  // 次のフレームの2投目がまだない場合は0を返す
                }
                return 10 + nextFrame.getFirstRoll() + nextFrame.getSecondRoll();
            }
        } else if (frame.isSpare()) {
            if (nextFrame == null || nextFrame.getFirstRoll() == null) {
                return 0;  // 次のフレームがまだない場合は0を返す
            }
            return 10 + nextFrame.getFirstRoll();
        }
        
        return 0;  // それ以外の場合（フレームが未完了など）
    }

    /**
     * ストライクのスコア計算に必要な次の2投が利用可能かを確認する。
     * - 次のフレームがストライクの場合：その次のフレームの1投目まで必要
     * - 次のフレームが通常の場合：2投目まで必要
     *
     * @param frame 確認対象のフレーム
     * @param frameIndex フレームの位置（0-9）
     * @param frames 全フレームのリスト
     * @return 次の2投が利用可能な場合true
     */
    private boolean hasNextRollsForStrike(Frame frame, int frameIndex, List<Frame> frames) {
        if (frameIndex >= 9) return false;
        Frame nextFrame = frames.get(frameIndex + 1);
        if (nextFrame.isStrike() && frameIndex < 8) {
            Frame nextNextFrame = frames.get(frameIndex + 2);
            return nextNextFrame.getFirstRoll() != null;
        }
        return nextFrame.getSecondRoll() != null;
    }


    /**
     * 指定されたIDのゲームを取得する。
     *
     * @param gameId 取得するゲームのID
     * @return 取得したゲーム情報
     * @throws IllegalArgumentException 指定されたIDのゲームが存在しない場合
     */
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    /**
     * 指定されたゲームの全フレームをフレーム番号順で取得する。
     *
     * @param gameId フレームを取得するゲームのID
     * @return フレーム番号順に並んだフレームのリスト
     */
    public List<Frame> getFrames(Long gameId) {
        return frameRepository.findByGameIdOrderByFrameNumber(gameId);
    }
}
