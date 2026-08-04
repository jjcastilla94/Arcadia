package com.arcadia.game.controller;

import com.arcadia.common.response.ApiResponse;
import com.arcadia.game.dto.response.CategoryResponse;
import com.arcadia.game.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Game categories")
public class CategoryController {

    private final GameService gameService;

    public CategoryController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping
    @Operation(summary = "List categories", description = "Returns all game categories sorted by name")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> categories() {
        return ResponseEntity.ok(ApiResponse.ok("Categories", gameService.listCategories()));
    }
}
