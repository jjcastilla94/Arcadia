package com.arcadia.admin.dto.response;

import com.arcadia.game.dto.response.CategoryResponse;

import java.time.LocalDateTime;

public record AdminGameResponse(
        Long id,
        String title,
        String slug,
        String description,
        String thumbnailPath,
        String coverUrl,
        String filePath,
        String version,
        Long fileSize,
        CategoryResponse category,
        boolean isPublic,
        boolean isHidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
