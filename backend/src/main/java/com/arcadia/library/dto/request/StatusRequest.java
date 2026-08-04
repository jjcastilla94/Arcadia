package com.arcadia.library.dto.request;

import com.arcadia.entity.LibraryStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull LibraryStatus status
) {
}
