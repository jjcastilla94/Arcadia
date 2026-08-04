package com.arcadia.library.dto.response;

import com.arcadia.entity.LibraryStatus;

import java.time.LocalDateTime;

public record LibraryItemResponse(
        Long id,
        LibraryGameResponse game,
        LibraryStatus status,
        Integer rating,
        LocalDateTime addedAt,
        LocalDateTime lastPlayedAt,
        long timePlayedSeconds
) {
}
