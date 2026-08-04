package com.arcadia.session.controller;

import com.arcadia.common.response.ApiResponse;
import com.arcadia.security.CustomUserDetails;
import com.arcadia.session.dto.request.EndSessionRequest;
import com.arcadia.session.dto.request.StartSessionRequest;
import com.arcadia.session.dto.response.PlaySessionResponse;
import com.arcadia.session.service.SessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/play-sessions")
@Tag(name = "Play Sessions", description = "Game session tracking (start/end)")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start a play session", description = "Registers the start of a game session for the user")
    public ResponseEntity<ApiResponse<PlaySessionResponse>> start(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody StartSessionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Session started", sessionService.start(principal.getUser(), request.gameId())));
    }

    @PostMapping("/end")
    @Operation(summary = "End a play session",
            description = "Closes the session, stores its duration and updates the library time played")
    public ResponseEntity<ApiResponse<PlaySessionResponse>> end(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody EndSessionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Session ended", sessionService.end(principal.getUser(), request.sessionId())));
    }
}
