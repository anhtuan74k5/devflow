package com.example.devflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for all REST endpoints.
 * <p>
 * Ensures consistent response structure across the entire API:
 * {@code { "success": true/false, "message": "...", "data": {...} }}
 * This allows clients to handle success/failure uniformly without parsing HTTP status codes.
 *
 * @param <T> the type of the data payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * Creates a success response with data and a default message.
     *
     * @param data the response payload
     * @param <T>  the type of the payload
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operation successful")
                .data(data)
                .build();
    }

    /**
     * Creates a success response with custom message and data.
     *
     * @param message the success message
     * @param data    the response payload
     * @param <T>     the type of the payload
     * @return ApiResponse with success=true
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error response with a message.
     *
     * @param message the error description
     * @param <T>     the type of the payload (usually Void)
     * @return ApiResponse with success=false
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
