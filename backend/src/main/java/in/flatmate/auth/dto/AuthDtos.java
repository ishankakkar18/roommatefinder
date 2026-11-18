package in.flatmate.auth.dto;

import in.flatmate.user.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank
            @Email(message = "must be a valid email")
            @Size(max = 120)
            String email,

            @NotBlank
            @Size(min = 8, max = 72, message = "must be 8-72 characters")
            String password,

            @NotBlank
            @Size(min = 2, max = 40, message = "must be 2-40 characters")
            String name,

            Gender gender,

            @Pattern(regexp = "^[6-9]\\d{9}$", message = "must be a 10-digit mobile number")
            String phone
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email(message = "must be a valid email") String email,
            @NotBlank String password
    ) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record TokenResponse(
            String accessToken,
            String refreshToken,
            long expiresInSeconds,
            boolean profileComplete
    ) {
    }

    public record MeResponse(
            Long id,
            String email,
            String phone,
            String name,
            String gender,
            boolean profileComplete
    ) {
    }
}
