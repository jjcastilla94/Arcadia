package com.arcadia.auth.dto.response;

import java.util.Set;

public record UserResponse(
        Long id,
        String nickname,
        String email,
        String avatarUrl,
        boolean emailVerified,
        Set<String> roles
) {
}
