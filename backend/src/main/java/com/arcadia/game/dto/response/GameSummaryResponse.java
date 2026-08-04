package com.arcadia.game.dto.response;

import java.time.LocalDateTime;

public record GameSummaryResponse(
        Long id,
        String title,
        String slug,
        String description,
        String thumbnailPath,
        String coverUrl,
        String version,
        CategoryResponse category,
        LocalDateTime createdAt,
        long playCount
) {
}
