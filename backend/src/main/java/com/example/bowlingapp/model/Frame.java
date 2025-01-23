package com.example.bowlingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

/**
 * ボウリングの1フレームを表現するエンティティクラス。
 * 各フレームは1-2回の投球（10フレーム目は最大3回）を記録し、
 * そのフレームのスコアを保持する。
 */
@Entity
@Data
public class Frame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** フレーム番号（1-10） */
    private int frameNumber;
    
    /** 1投目のピン数（0-10） */
    private Integer firstRoll;
    
    /** 2投目のピン数（0-10） */
    private Integer secondRoll;
    
    /** 10フレーム目の3投目のピン数（0-10） */
    private Integer thirdRoll;
    
    /** このフレームまでの合計スコア */
    private int frameScore;

    /** このフレームが属するゲーム */
    @ManyToOne
    @JsonIgnore
    private Game game;

    /**
     * このフレームがストライクかどうかを判定する。
     * ストライクは1投目で10ピンすべてを倒した場合。
     *
     * @return 1投目が10ピンの場合true
     */
    public boolean isStrike() {
        return firstRoll != null && firstRoll == 10;
    }

    /**
     * このフレームがスペアかどうかを判定する。
     * スペアは2投の合計が10ピンで、かつストライクでない場合。
     *
     * @return 1投目と2投目の合計が10ピンで、ストライクでない場合true
     */
    public boolean isSpare() {
        return !isStrike() && firstRoll != null && secondRoll != null && 
               (firstRoll + secondRoll == 10);
    }

    /**
     * このフレームが完了しているかどうかを判定する。
     * フレームの完了条件：
     * - 通常フレーム（1-9フレーム）
     *   - ストライクの場合：1投目のみで完了
     *   - 非ストライクの場合：2投目まで必要
     * - 10フレーム目
     *   - ストライク/スペアの場合：3投必要
     *   - 通常の場合：2投で完了
     *
     * @return フレームが完了している場合true
     */
    public boolean isComplete() {
        if (frameNumber == 10) {
            if (isStrike() || isSpare()) {
                return firstRoll != null && secondRoll != null && thirdRoll != null;
            }
            return firstRoll != null && secondRoll != null;
        }
        return isStrike() || (firstRoll != null && secondRoll != null);
    }
}
