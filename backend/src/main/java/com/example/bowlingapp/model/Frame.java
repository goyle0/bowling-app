package com.example.bowlingapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Frame {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int frameNumber;
    private Integer firstRoll;
    private Integer secondRoll;
    private Integer thirdRoll;  // 10フレーム目用
    private int frameScore;

    @ManyToOne
    @JsonIgnore
    private Game game;

    public boolean isStrike() {
        return firstRoll != null && firstRoll == 10;
    }

    public boolean isSpare() {
        return !isStrike() && firstRoll != null && secondRoll != null && 
               (firstRoll + secondRoll == 10);
    }

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
