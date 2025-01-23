package com.example.bowlingapp.repository;

import com.example.bowlingapp.model.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * ボウリングフレームエンティティのデータアクセスを提供するリポジトリインターフェース。
 * JpaRepositoryを継承することで、基本的なCRUD操作と
 * ページネーション機能を利用できる。
 */
@Repository
public interface FrameRepository extends JpaRepository<Frame, Long> {
    
    /**
     * 指定されたゲームIDに属するフレームをフレーム番号順で取得する。
     * 1ゲームの全フレームを投球順に取得するために使用する。
     *
     * @param gameId 取得対象のゲームID
     * @return フレーム番号順に並べられたフレームのリスト
     */
    List<Frame> findByGameIdOrderByFrameNumber(Long gameId);
}
