package com.example.devflow.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Utility class for JWT token generation and validation.
 * <p>
 * Generates two types of tokens: short-lived access tokens (15 min by default)
 * for API authentication and long-lived refresh tokens (7 days by default)
 * for obtaining new access tokens without re-login. Separated from
 * JwtAuthenticationFilter to follow Single Responsibility — this class handles
 * only cryptographic operations, while the filter handles request interception.
 * Secret key is injected from environment variable to avoid hardcoding secrets.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final SecretKey secretKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms:900000}") long accessExpirationMs,
                   @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    /**
     * Generates a short-lived access token for API authentication.
     * <p>
     * Access tokens are sent in the Authorization header as Bearer tokens.
     * Short expiry (default 15 min) limits the damage window if the token is leaked.
     *
     * @param username the authenticated user's username
     * @return a signed JWT access token string
     */
    public String generateAccessToken(String username) {
        return buildToken(username, accessExpirationMs);
    }

    /**
     * Generates a long-lived refresh token for obtaining new access tokens.
     * <p>
     * Refresh tokens are never sent as Bearer tokens — they are sent only
     * to the /api/auth/refresh endpoint. Longer expiry (default 7 days)
     * allows users to stay logged in without re-entering credentials.
     *
     * @param username the authenticated user's username
     * @return a signed JWT refresh token string
     */
    public String generateRefreshToken(String username) {
        return buildToken(username, refreshExpirationMs);
    }

    /**
     * Extracts the username (subject) from any JWT token.
     *
     * @param token the JWT string
     * @return the username embedded in the token, or null if parsing fails
     */
    public String getUsernameFromToken(String token) {
        try {
            return parseClaims(token).getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract username from token: {}", e.getMessage());
            return null;
        }
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
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired");
            return false;
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extracts a specific claim from a JWT token.
     *
     * @param token     the JWT string
     * @param claimName the claim key to extract
     * @return the claim value as a String, or null if not found
     */
    public String getClaim(String token, String claimName) {
        try {
            return parseClaims(token).get(claimName, String.class);
        } catch (Exception e) {
            return null;
        }
    }

    private String buildToken(String username, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
