package com.example.devflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful authentication.
 * <p>
 * Contains both an access token (short-lived, for API calls via Bearer header)
 * and a refresh token (long-lived, for obtaining new access tokens).
 * The {@code token} field is kept as a backward-compatible alias for {@code accessToken}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String token;
    private String accessToken;
    private String refreshToken;
    private String username;
    private String role;
}
