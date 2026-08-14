package com.arcadia.progress.dto.request;

import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

public record SaveProgressRequest(
        @NotNull Long gameId,
        @NotNull JsonNode data
) {
}
