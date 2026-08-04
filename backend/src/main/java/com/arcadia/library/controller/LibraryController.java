package com.arcadia.library.controller;

import com.arcadia.common.response.ApiResponse;
import com.arcadia.library.dto.request.RatingRequest;
import com.arcadia.library.dto.request.StatusRequest;
import com.arcadia.library.dto.response.LibraryItemResponse;
import com.arcadia.library.service.LibraryService;
import com.arcadia.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/library")
@Tag(name = "Library", description = "Authenticated user game library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/my-games")
    @Operation(summary = "Get my library", description = "Returns the games added by the authenticated user")
    public ResponseEntity<ApiResponse<List<LibraryItemResponse>>> myGames(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok("My library", libraryService.getMyLibrary(principal.getUser())));
    }

    @GetMapping("/{gameId}")
    @Operation(summary = "Check if a game is in the library")
    public ResponseEntity<ApiResponse<Boolean>> inLibrary(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long gameId) {
        return ResponseEntity.ok(ApiResponse.ok("In library", libraryService.isInLibrary(principal.getUser(), gameId)));
    }

    @PostMapping("/add/{gameId}")
    @Operation(summary = "Add a game to the library")
    public ResponseEntity<ApiResponse<LibraryItemResponse>> add(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long gameId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Game added to library", libraryService.addToLibrary(principal.getUser(), gameId)));
    }

    @DeleteMapping("/remove/{gameId}")
    @Operation(summary = "Remove a game from the library")
    public ResponseEntity<ApiResponse<Void>> remove(
            @AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long gameId) {
        libraryService.removeFromLibrary(principal.getUser(), gameId);
        return ResponseEntity.ok(ApiResponse.ok("Game removed from library"));
    }

    @PutMapping("/{gameId}/status")
    @Operation(summary = "Update the play status of a library game")
    public ResponseEntity<ApiResponse<LibraryItemResponse>> updateStatus(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long gameId,
            @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated",
                libraryService.updateStatus(principal.getUser(), gameId, request.status())));
    }

    @PutMapping("/{gameId}/rating")
    @Operation(summary = "Rate a library game from 1 to 5")
    public ResponseEntity<ApiResponse<LibraryItemResponse>> updateRating(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long gameId,
            @Valid @RequestBody RatingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Rating updated",
                libraryService.updateRating(principal.getUser(), gameId, request.rating())));
    }
}
