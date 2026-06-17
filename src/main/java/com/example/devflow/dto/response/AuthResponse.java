package com.example.devflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO returned after successful authentication.
 * <p>
 * Contains the JWT token and basic user info. The token is a signed JWT
 * that the client must include in subsequent requests via the Authorization header.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String role;
}
