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

/**
 * ボウリングゲーム全体を表現するエンティティクラス。
 * 1ゲームは10フレームで構成され、各フレームのスコアと
 * ゲーム全体の合計スコアを管理する。
 */
@Entity
@Data
public class Game {
    /** ゲームの一意識別子 */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** このゲームに属する全フレームのリスト */
    @OneToMany(mappedBy = "game")
    private List<Frame> frames = new ArrayList<>();

    /** ゲームの合計スコア */
    private int totalScore;

    /** ゲームの作成日時 */
    private LocalDateTime createdAt;

    /** ゲームが完了したかどうか */
    private boolean completed;

    /**
     * 新しいゲームを作成する。
     * 作成時刻を現在時刻に設定し、完了フラグをfalseに設定する。
     */
    public Game() {
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }
}
