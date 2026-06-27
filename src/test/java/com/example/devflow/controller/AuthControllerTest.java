package com.example.devflow.controller;

import com.example.devflow.config.TestSecurityConfig;
import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.exception.BusinessException;
import com.example.devflow.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/auth/register: valid request → 201 with token")
    void register_success() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .username("newuser")
                .role("ROLE_USER")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /api/auth/register: duplicate username → 400")
    void register_fail_duplicate() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("existing")
                .password("password123")
                .build();

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new BusinessException("Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    @DisplayName("POST /api/auth/register: blank username → 400 validation error")
    void register_fail_validation() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login: valid credentials → 200 with token")
    void login_success() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("correctPassword")
                .build();

        AuthResponse authResponse = AuthResponse.builder()
                .token("jwt-token")
                .username("validuser")
                .role("ROLE_USER")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    @DisplayName("POST /api/auth/login: wrong password → 401")
    void login_fail_badCredentials() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("wrongPassword")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
}
