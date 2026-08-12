package com.arcadia.user.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
        String nickname,

        @Size(max = 255, message = "Avatar URL must be at most 255 characters")
        String avatarUrl
) {
}
