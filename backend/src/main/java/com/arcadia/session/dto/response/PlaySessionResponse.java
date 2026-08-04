package com.arcadia.session.dto.response;

import java.time.LocalDateTime;

public record PlaySessionResponse(
        Long id,
        Long gameId,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Long durationSeconds
) {
}
