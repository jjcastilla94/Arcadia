package com.arcadia.session.dto.request;

import jakarta.validation.constraints.NotNull;

public record StartSessionRequest(
        @NotNull Long gameId
) {
}
