package com.arcadia.game.dto.response;

public record GameImageResponse(
        Long id,
        String imageUrl,
        int position
) {
}
