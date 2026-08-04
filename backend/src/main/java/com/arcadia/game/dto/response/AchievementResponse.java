package com.arcadia.game.dto.response;

public record AchievementResponse(
        Long id,
        String title,
        String description,
        String icon,
        int points,
        boolean hidden
) {
}
