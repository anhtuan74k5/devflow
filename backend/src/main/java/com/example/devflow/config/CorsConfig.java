package com.example.devflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the DevFlow API.
 * <p>
 * Allows requests from the Vite dev server (http://localhost:5173) during frontend development.
 * In production, the Nginx reverse proxy handles CORS instead of this config.
 * The {@code allowCredentials(true)} is required for sending the JWT Bearer token via cookies/Authorization header.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
