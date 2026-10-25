package odk.malicompetences.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank(message = "Le refresh token est obligatoire")
        String refreshToken
) {}
