package com.example.devflow.dto.response;

import com.example.devflow.model.TaskPriority;
import com.example.devflow.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for task data.
 * <p>
 * Includes assignee info inline for convenience. Decouples API contract
 * from the Task entity to avoid exposing JPA internals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long projectId;
    private Long assigneeId;
    private String assigneeUsername;
}
