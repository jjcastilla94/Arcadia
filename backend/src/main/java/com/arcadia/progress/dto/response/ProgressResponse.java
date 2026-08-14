package com.arcadia.progress.dto.response;

import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ProgressResponse(
        Long gameId,
        JsonNode data,
        LocalDateTime updatedAt
) {
}
