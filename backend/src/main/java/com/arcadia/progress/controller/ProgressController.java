package com.arcadia.progress.controller;

import com.arcadia.common.response.ApiResponse;
import com.arcadia.progress.dto.request.SaveProgressRequest;
import com.arcadia.progress.dto.response.ProgressResponse;
import com.arcadia.progress.service.ProgressService;
import com.arcadia.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progress", description = "Per-user cloud saves for games (Phase 4)")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping("/save")
    @Operation(summary = "Save game progress", description = "Creates or updates the JSON save of the authenticated user for a game")
    public ResponseEntity<ApiResponse<ProgressResponse>> save(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody SaveProgressRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Progress saved", progressService.save(principal.getUser(), request)));
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Get game progress", description = "Returns the JSON save of the authenticated user for a game")
    public ResponseEntity<ApiResponse<ProgressResponse>> get(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long gameId) {
        return ResponseEntity.ok(ApiResponse.ok("Progress loaded", progressService.get(principal.getUser(), gameId)));
    }
}
