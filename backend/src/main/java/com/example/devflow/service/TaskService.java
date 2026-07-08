package com.example.devflow.service;

import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for task management operations.
 * <p>
 * Tasks are always scoped within a project. Access control checks that
 * the authenticated user is the project owner before allowing operations.
 * The method name {@code updateTaskStatus} is the AOP pointcut target
 * for automatic activity logging — do not rename it.
 */
public interface TaskService {

    /**
     * Retrieves all tasks for a project with optional status filter.
     *
     * @param projectId the project ID
     * @param status    optional status filter (null for all)
     * @param pageable  pagination parameters
     * @return a page of task responses
     */
    Page<TaskResponse> getTasksByProject(Long projectId, TaskStatus status, Pageable pageable);

    /**
     * Retrieves a single task by its ID within a project.
     *
     * @param projectId the project ID
     * @param taskId    the task ID
     * @return the task response
     */
    TaskResponse getTaskById(Long projectId, Long taskId);

    /**
     * Creates a new task within a project.
     *
     * @param projectId the project ID
     * @param request   the task creation details
     * @return the created task response
     */
    TaskResponse createTask(Long projectId, CreateTaskRequest request);

    /**
     * Updates an existing task.
     *
     * @param projectId the project ID
     * @param taskId    the task ID
     * @param request   the updated task details
     * @return the updated task response
     */
    TaskResponse updateTask(Long projectId, Long taskId, CreateTaskRequest request);

    /**
     * Updates only the status of a task.
     * <p>
     * This method is the target of the AOP @AfterReturning advice in LoggingAspect.
     * The aspect automatically creates an ActivityLog entry after successful execution.
     *
     * @param projectId the project ID
     * @param taskId    the task ID
     * @param request   the new status
     * @return the updated task response
     */
    TaskResponse updateTaskStatus(Long projectId, Long taskId, UpdateTaskStatusRequest request);

    /**
     * Deletes a task.
     *
     * @param projectId the project ID
     * @param taskId    the task ID
     */
    void deleteTask(Long projectId, Long taskId);
}
