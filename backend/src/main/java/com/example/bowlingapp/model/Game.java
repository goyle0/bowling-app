package com.example.bowlingapp.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "game")
    private List<Frame> frames = new ArrayList<>();

    private int totalScore;
    private LocalDateTime createdAt;
    private boolean completed;

    public Game() {
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }
}
