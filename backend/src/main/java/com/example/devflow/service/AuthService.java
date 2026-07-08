package com.example.devflow.service;

import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RefreshTokenRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.entity.User;

/**
 * Service interface for authentication operations.
 * <p>
 * Separated from other services to follow Interface Segregation and
 * Single Responsibility — this service only handles user registration,
 * login, token refresh, and current user retrieval.
 */
public interface AuthService {

    /**
     * Registers a new user with the given credentials.
     * <p>
     * Encrypts the password using BCrypt before persisting.
     * Returns both an access token and a refresh token so the user
     * is immediately authenticated without an additional login call.
     *
     * @param request the registration details
     * @return AuthResponse containing access token, refresh token, and user info
     * @throws com.example.devflow.exception.BusinessException if username already exists
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with username and password.
     * <p>
     * Delegates password matching to Spring Security's AuthenticationManager.
     * Returns a signed access token (short-lived) and a refresh token (long-lived).
     *
     * @param request the login credentials
     * @return AuthResponse containing access token, refresh token, and user info
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);

    /**
     * Issues a new access token using a valid refresh token.
     * <p>
     * Implements token rotation: both the access token and refresh token
     * are renewed. The old refresh token is no longer usable after this call.
     * This limits the damage window if a refresh token is leaked.
     *
     * @param request containing the refresh token
     * @return AuthResponse with new access token and new refresh token
     * @throws com.example.devflow.exception.BusinessException if the refresh token is invalid or expired
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Retrieves the currently authenticated user from the SecurityContext.
     * <p>
     * This is the ONLY place in the codebase where SecurityContextHolder is accessed,
     * centralizing security context access and making it easier to mock in tests.
     *
     * @return the authenticated User entity
     * @throws com.example.devflow.exception.ResourceNotFoundException if the user is not found
     */
    User getCurrentUser();
}
