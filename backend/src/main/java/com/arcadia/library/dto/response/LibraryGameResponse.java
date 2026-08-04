package com.arcadia.library.dto.response;

import com.arcadia.game.dto.response.CategoryResponse;

public record LibraryGameResponse(
        Long id,
        String title,
        String slug,
        String thumbnailPath,
        String coverUrl,
        String version,
        CategoryResponse category
) {
}
