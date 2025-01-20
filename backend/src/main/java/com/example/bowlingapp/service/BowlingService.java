package com.example.bowlingapp.service;

import com.example.bowlingapp.model.Frame;
import com.example.bowlingapp.model.Game;
import com.example.bowlingapp.repository.FrameRepository;
import com.example.bowlingapp.repository.GameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BowlingService {
    private final GameRepository gameRepository;
    private final FrameRepository frameRepository;

    public BowlingService(GameRepository gameRepository, FrameRepository frameRepository) {
        this.gameRepository = gameRepository;
        this.frameRepository = frameRepository;
    }

    @Transactional
    public Game createNewGame() {
        Game game = new Game();
        game = gameRepository.save(game);
        
        // 10フレームを初期化
        for (int i = 1; i <= 10; i++) {
            Frame frame = new Frame();
            frame.setFrameNumber(i);
            frame.setGame(game);
            frameRepository.save(frame);
        }
        
        return game;
    }

    @Transactional
    public Game recordRoll(Long gameId, int frameNumber, int pins) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
                
        List<Frame> frames = frameRepository.findByGameIdOrderByFrameNumber(gameId);
        Frame currentFrame = frames.stream()
                .filter(f -> f.getFrameNumber() == frameNumber)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Frame not found"));

        if (currentFrame.getFirstRoll() == null) {
            currentFrame.setFirstRoll(pins);
        } else if (currentFrame.getSecondRoll() == null) {
            currentFrame.setSecondRoll(pins);
        } else if (frameNumber == 10 && 
                  (currentFrame.isStrike() || currentFrame.isSpare()) && 
                  currentFrame.getThirdRoll() == null) {
            currentFrame.setThirdRoll(pins);
        }

        calculateScores(frames);
        game.setTotalScore(frames.get(9).getFrameScore());
        
        // ゲーム終了判定
        boolean isComplete = frames.get(9).isComplete();
        game.setCompleted(isComplete);

        frameRepository.save(currentFrame);
        return gameRepository.save(game);
    }

    private void calculateScores(List<Frame> frames) {
        int runningScore = 0;
        
        for (int i = 0; i < frames.size(); i++) {
            Frame frame = frames.get(i);
            if (frame.getFirstRoll() == null) break;

            if (i == 9) {
                // 10フレーム目の計算
                if (frame.isStrike()) {
                    if (frame.getSecondRoll() != null && frame.getThirdRoll() != null) {
                        frame.setFrameScore(runningScore + 10 + frame.getSecondRoll() + frame.getThirdRoll());
                    }
                } else if (frame.isSpare()) {
                    if (frame.getThirdRoll() != null) {
                        frame.setFrameScore(runningScore + 10 + frame.getThirdRoll());
                    }
                } else if (frame.getSecondRoll() != null) {
                    frame.setFrameScore(runningScore + frame.getFirstRoll() + frame.getSecondRoll());
                }
            } else {
                // 1-9フレームの計算
                Optional<Frame> nextFrame = Optional.ofNullable(i < frames.size() - 1 ? frames.get(i + 1) : null);
                Optional<Frame> nextNextFrame = Optional.ofNullable(i < frames.size() - 2 ? frames.get(i + 2) : null);

                if (frame.isStrike()) {
                    if (nextFrame.isPresent() && nextFrame.get().getFirstRoll() != null) {
                        if (nextFrame.get().isStrike() && nextNextFrame.isPresent() && nextNextFrame.get().getFirstRoll() != null) {
                            frame.setFrameScore(runningScore + 20 + nextNextFrame.get().getFirstRoll());
                            runningScore = frame.getFrameScore();
                        } else if (nextFrame.get().getSecondRoll() != null) {
                            frame.setFrameScore(runningScore + 10 + nextFrame.get().getFirstRoll() + nextFrame.get().getSecondRoll());
                            runningScore = frame.getFrameScore();
                        }
                    }
                } else if (frame.getSecondRoll() != null) {
                    if (frame.isSpare()) {
                        if (nextFrame.isPresent() && nextFrame.get().getFirstRoll() != null) {
                            frame.setFrameScore(runningScore + 10 + nextFrame.get().getFirstRoll());
                            runningScore = frame.getFrameScore();
                        }
                    } else {
                        frame.setFrameScore(runningScore + frame.getFirstRoll() + frame.getSecondRoll());
                        runningScore = frame.getFrameScore();
                    }
                }
            }
        }
    }

    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
    }

    public List<Frame> getFrames(Long gameId) {
        return frameRepository.findByGameIdOrderByFrameNumber(gameId);
    }
}
