package com.arcadia.game.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record GameDetailsResponse(
        Long id,
        String title,
        String slug,
        String description,
        String thumbnailPath,
        String coverUrl,
        String version,
        CategoryResponse category,
        LocalDateTime createdAt,
        Long fileSize,
        List<GameImageResponse> images,
        List<GameVersionResponse> versions,
        List<AchievementResponse> achievements,
        long playCount,
        long playerCount,
        long totalPlayTimeSeconds
) {
}
