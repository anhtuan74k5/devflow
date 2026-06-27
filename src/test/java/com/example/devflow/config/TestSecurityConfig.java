package com.example.devflow.config;

import com.example.devflow.config.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test configuration that disables JWT authentication for controller tests.
 * <p>
 * Replaces the real SecurityConfig with a minimal stateless config that permits
 * all requests — tests rely on @WithMockUser for authorization simulation.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil("dGVzdC1zZWNyZXQta2V5LWZvci1kZXZmbG93LWFwcGxpY2F0aW9uLXRlc3Rpbmc=", 86400000L);
    }
}
