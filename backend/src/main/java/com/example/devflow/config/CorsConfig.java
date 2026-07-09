package com.example.devflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the DevFlow API.
 * <p>
 * In development, the Vite dev server (http://localhost:5173) proxies API requests.
 * In production, the Nginx reverse proxy handles requests from the same origin,
 * so CORS is only relevant for development or when frontend/backend are on different origins.
 * <p>
 * The allowed origins are injected via the {@code cors.allowed-origins} property,
 * which can be set per environment (e.g., localhost:5173 for dev, domain.com for prod).
 * Multiple origins can be comma-separated.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
