package com.example.bowlingapp.repository;

import com.example.bowlingapp.model.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FrameRepository extends JpaRepository<Frame, Long> {
    List<Frame> findByGameIdOrderByFrameNumber(Long gameId);
}
