package com.example.devflow.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token generation and validation.
 * <p>
 * Separated from JwtAuthenticationFilter to follow Single Responsibility:
 * this class handles only cryptographic operations, while the filter
 * handles only request interception. Secret key is injected from environment
 * variable to avoid hardcoding secrets in source code.
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:86400000}") long expirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Generates a JWT token for the given username.
     * <p>
     * Token includes the username as subject and an expiration time.
     * The signing key is derived from the configured secret using HMAC-SHA.
     *
     * @param username the authenticated user's username
     * @return a signed JWT string
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Extracts the username (subject) from a JWT token.
     *
     * @param token the JWT string
     * @return the username embedded in the token
     */
    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Validates a JWT token by checking its signature and expiration.
     *
     * @param token the JWT string to validate
     * @return true if the token is valid and not expired
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
