package com.example.devflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for project data.
 * <p>
 * Decouples the API contract from the Project entity, preventing
 * accidental exposure of internal fields (e.g., JPA lazy proxies).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private Long ownerId;
    private String ownerUsername;
}
