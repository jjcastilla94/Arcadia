package com.arcadia.admin.controller;

import com.arcadia.admin.dto.response.AdminGameResponse;
import com.arcadia.admin.service.AdminGameService;
import com.arcadia.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/games")
@Tag(name = "Admin", description = "Admin-only game management (upload, edit, publish, delete)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGameController {

    private final AdminGameService adminGameService;

    public AdminGameController(AdminGameService adminGameService) {
        this.adminGameService = adminGameService;
    }

    @GetMapping
    @Operation(summary = "List all games for management",
            description = "Returns every game including unpublished and hidden ones")
    public ResponseEntity<ApiResponse<List<AdminGameResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok("Games", adminGameService.listAll()));
    }

    @PostMapping
    @Operation(summary = "Upload a game",
            description = "Uploads a ZIP with index.html plus optional thumbnail/cover and creates the game")
    public ResponseEntity<ApiResponse<AdminGameResponse>> create(
            @RequestParam("zip") MultipartFile zip,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String releaseNotes,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile cover,
            @RequestParam(required = false) Boolean isPublic) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Game created",
                        adminGameService.create(zip, title, description, categoryId, version,
                                releaseNotes, thumbnail, cover, isPublic)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a game",
            description = "Edits metadata, toggles public/hidden and optionally uploads a new version (zip + version)")
    public ResponseEntity<ApiResponse<AdminGameResponse>> update(
            @PathVariable Long id,
            @RequestParam(required = false) MultipartFile zip,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) String releaseNotes,
            @RequestParam(required = false) MultipartFile thumbnail,
            @RequestParam(required = false) MultipartFile cover,
            @RequestParam(required = false) Boolean isPublic,
            @RequestParam(required = false) Boolean isHidden) {
        return ResponseEntity.ok(ApiResponse.ok("Game updated",
                adminGameService.update(id, zip, title, description, categoryId, version,
                        releaseNotes, thumbnail, cover, isPublic, isHidden)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a game", description = "Deletes the game files and its database record")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        adminGameService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Game deleted"));
    }
}
