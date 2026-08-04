package com.arcadia.game.dto.response;

import java.time.LocalDateTime;

public record GameVersionResponse(
        String version,
        String releaseNotes,
        LocalDateTime uploadedAt
) {
}
