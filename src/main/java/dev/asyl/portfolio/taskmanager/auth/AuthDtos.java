package dev.asyl.portfolio.taskmanager.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 80) String username,
            @NotBlank @Size(min = 8, max = 72) String password
    ) {
    }

    public record LoginRequest(
            @NotBlank @Size(max = 80) String username,
            @NotBlank @Size(max = 72) String password
    ) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
    }

    public record UserResponse(Long id, String username, AccountRole role, Instant createdAt) {

        public static UserResponse from(AppUser user) {
            return new UserResponse(user.getId(), user.getUsername(), user.getRole(), user.getCreatedAt());
        }
    }
}
