package com.example.bowlingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ボウリングゲームアプリケーションのメインクラス。
 * Spring Bootアプリケーションのエントリーポイントとして機能し、
 * アプリケーションの起動と初期化を担当する。
 */
@SpringBootApplication
public class BowlingApplication {
    
    /**
     * アプリケーションのメインメソッド。
     * Spring Bootアプリケーションを起動し、必要な設定とコンポーネントの
     * 初期化を行う。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(BowlingApplication.class, args);
    }
}
