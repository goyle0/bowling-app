package com.example.bowlingapp.repository;

import com.example.bowlingapp.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * ボウリングゲームエンティティのデータアクセスを提供するリポジトリインターフェース。
 * JpaRepositoryを継承することで、基本的なCRUD操作と
 * ページネーション機能を利用できる。
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
}
