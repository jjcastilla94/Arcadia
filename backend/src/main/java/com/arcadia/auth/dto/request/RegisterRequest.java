package com.arcadia.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nickname is required")
        @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
        String nickname,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String password
) {
}
