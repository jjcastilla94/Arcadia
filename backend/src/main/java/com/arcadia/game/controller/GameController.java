package com.arcadia.game.controller;

import com.arcadia.common.response.ApiResponse;
import com.arcadia.game.dto.response.GameDetailsResponse;
import com.arcadia.game.dto.response.GameSummaryResponse;
import com.arcadia.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Games", description = "Public game catalog")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    @Operation(summary = "List catalog games",
            description = "Returns public games, optionally filtered by search term and category slug")
    public ResponseEntity<ApiResponse<List<GameSummaryResponse>>> catalog(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok("Game catalog", gameService.listGames(search, category)));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get game details",
            description = "Returns the full details of a public game by slug, including images, versions and achievements")
    public ResponseEntity<ApiResponse<GameDetailsResponse>> details(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.ok("Game details", gameService.getBySlug(slug)));
    }
}
