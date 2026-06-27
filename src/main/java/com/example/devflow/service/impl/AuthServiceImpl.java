package com.example.devflow.service.impl;

import com.example.devflow.config.JwtUtil;
import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.entity.User;
import com.example.devflow.exception.BusinessException;
import com.example.devflow.model.Role;
import com.example.devflow.repository.UserRepository;
import com.example.devflow.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of AuthService.
 * <p>
 * Handles user registration with BCrypt password encoding and login
 * via Spring Security's AuthenticationManager. JWT tokens are generated
 * by JwtUtil. SecurityContextHolder is accessed only in getCurrentUser()
 * to centralize security context dependency.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank()) {
            throw new BusinessException("Username must not be blank");
        }
        if (username.length() < 3) {
            throw new BusinessException("Username must be at least 3 characters");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("Password must not be blank");
        }
        if (password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters");
        }

        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (username == null || username.isBlank()) {
            throw new BusinessException("Username must not be blank");
        }
        if (username.length() < 3) {
            throw new BusinessException("Username must be at least 3 characters");
        }
        if (password == null || password.isBlank()) {
            throw new BusinessException("Password must not be blank");
        }
        if (password.length() < 6) {
            throw new BusinessException("Password must be at least 6 characters");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("Authentication required");
        }
        return (User) authentication.getPrincipal();
    }
}
