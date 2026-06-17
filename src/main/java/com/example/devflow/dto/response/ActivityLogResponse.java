package com.example.devflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for activity log entries.
 * <p>
 * Contains the activity content and timestamp. The content is a human-readable
 * string describing what happened (e.g., "Task 'Fix login' status changed from TODO to DONE").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private Long projectId;
}
