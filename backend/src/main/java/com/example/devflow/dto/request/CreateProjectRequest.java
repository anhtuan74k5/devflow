package com.example.devflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new project.
 * <p>
 * Only name is required; description is optional.
 * Validation ensures the project name is meaningful before reaching the service layer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequest {

    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 100, message = "Project name must be between 1 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
