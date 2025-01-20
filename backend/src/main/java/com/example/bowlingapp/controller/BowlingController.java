package com.example.bowlingapp.controller;

import com.example.bowlingapp.model.Frame;
import com.example.bowlingapp.model.Game;
import com.example.bowlingapp.service.BowlingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080")
public class BowlingController {
    private final BowlingService bowlingService;

    public BowlingController(BowlingService bowlingService) {
        this.bowlingService = bowlingService;
    }

    @PostMapping("/games")
    public ResponseEntity<Game> createGame() {
        return ResponseEntity.ok(bowlingService.createNewGame());
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<Game> getGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(bowlingService.getGame(gameId));
    }

    @GetMapping("/games/{gameId}/frames")
    public ResponseEntity<List<Frame>> getFrames(@PathVariable Long gameId) {
        return ResponseEntity.ok(bowlingService.getFrames(gameId));
    }

    @PostMapping("/games/{gameId}/rolls")
    public ResponseEntity<Game> recordRoll(
            @PathVariable Long gameId,
            @RequestBody Map<String, Integer> rollInfo) {
        
        int frameNumber = rollInfo.get("frameNumber");
        int pins = rollInfo.get("pins");
        
        return ResponseEntity.ok(bowlingService.recordRoll(gameId, frameNumber, pins));
    }
}
