package com.example.devflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for dashboard project statistics.
 * Contains task counts grouped by status for a single project.
 * Designed to replace the N+1 frontend pattern where each project
 * required 3 separate API calls to get TODO/IN_PROGRESS/DONE counts.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatsResponse {
    private Long projectId;
    private String projectName;
    private long total;
    private long todo;
    private long inProgress;
    private long done;
}
