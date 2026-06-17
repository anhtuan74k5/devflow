package com.example.devflow.service;

import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.entity.User;

/**
 * Service interface for authentication operations.
 * <p>
 * Separated from other services to follow Interface Segregation and
 * Single Responsibility — this service only handles user registration,
 * login, and current user retrieval.
 */
public interface AuthService {

    /**
     * Registers a new user with the given credentials.
     * <p>
     * Encrypts the password using BCrypt before persisting.
     * Returns a JWT token so the user is immediately authenticated.
     *
     * @param request the registration details
     * @return AuthResponse containing JWT token and user info
     * @throws com.example.devflow.exception.BusinessException if username already exists
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user with username and password.
     * <p>
     * Delegates password matching to Spring Security's AuthenticationManager.
     * Returns a signed JWT token upon successful authentication.
     *
     * @param request the login credentials
     * @return AuthResponse containing JWT token and user info
     * @throws org.springframework.security.authentication.BadCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);

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
