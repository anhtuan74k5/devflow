package com.example.devflow.dto.request;

import com.example.devflow.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating only the status of a task.
 * <p>
 * Separate from full task update to make the PATCH endpoint explicit
 * and to serve as the AOP pointcut target for automatic activity logging.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskStatusRequest {

    @NotNull(message = "Status is required")
    private TaskStatus status;
}
