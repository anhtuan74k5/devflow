package com.example.devflow.service.impl;

import com.example.devflow.config.JwtUtil;
import com.example.devflow.dto.request.LoginRequest;
import com.example.devflow.dto.request.RegisterRequest;
import com.example.devflow.dto.response.AuthResponse;
import com.example.devflow.entity.User;
import com.example.devflow.exception.BusinessException;
import com.example.devflow.model.Role;
import com.example.devflow.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register: username not taken → returns AuthResponse with token")
    void register_success() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(jwtUtil.generateToken("newuser")).thenReturn("jwt-token-123");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("newuser");
        savedUser.setPassword("encodedPassword");
        savedUser.setRole(Role.ROLE_USER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());

        verify(userRepository).existsByUsername("newuser");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateToken("newuser");
    }

    @Test
    @DisplayName("register: username already taken → throws BusinessException")
    void register_fail_duplicateUsername() {
        RegisterRequest request = RegisterRequest.builder()
                .username("existinguser")
                .password("password123")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(request));

        assertEquals("Username already exists", exception.getMessage());

        verify(userRepository).existsByUsername("existinguser");
        verifyNoInteractions(passwordEncoder, jwtUtil);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login: correct credentials → returns AuthResponse with token")
    void login_success() {
        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("correctPassword")
                .build();

        User user = new User();
        user.setId(1L);
        user.setUsername("validuser");
        user.setPassword("encodedPassword");
        user.setRole(Role.ROLE_USER);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("validuser", "correctPassword")))
                .thenReturn(authentication);
        when(jwtUtil.generateToken("validuser")).thenReturn("jwt-token-456");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("validuser", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).generateToken("validuser");
    }

    @Test
    @DisplayName("login: wrong password → throws BadCredentialsException")
    void login_fail_badCredentials() {
        LoginRequest request = LoginRequest.builder()
                .username("validuser")
                .password("wrongPassword")
                .build();

        when(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("validuser", "wrongPassword")))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
                () -> authService.login(request));

        assertEquals("Bad credentials", exception.getMessage());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil);
    }

    @ParameterizedTest(name = "register: invalid input \"{0}\" / \"{1}\" → throws BusinessException")
    @CsvSource({
            "'', password123, Username must not be blank",
            "ab, password123, Username must be at least 3 characters",
            "validuser, '', Password must not be blank",
            "validuser, 12345, Password must be at least 6 characters"
    })
    @DisplayName("register: invalid input → throws BusinessException")
    void register_fail_invalidInput(String username, String password, String expectedMessage) {
        RegisterRequest request = RegisterRequest.builder()
                .username(username)
                .password(password)
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(request));

        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(userRepository, passwordEncoder, jwtUtil);
    }

    @ParameterizedTest(name = "login: invalid input \"{0}\" / \"{1}\" → throws BusinessException")
    @CsvSource({
            "'', password123, Username must not be blank",
            "ab, password123, Username must be at least 3 characters",
            "validuser, '', Password must not be blank",
            "validuser, 12345, Password must be at least 6 characters"
    })
    @DisplayName("login: invalid input → throws BusinessException")
    void login_fail_invalidInput(String username, String password, String expectedMessage) {
        LoginRequest request = LoginRequest.builder()
                .username(username)
                .password(password)
                .build();

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(request));

        assertEquals(expectedMessage, exception.getMessage());
        verifyNoInteractions(authenticationManager, jwtUtil);
    }
}
