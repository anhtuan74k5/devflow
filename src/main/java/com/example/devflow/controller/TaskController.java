package com.example.devflow.controller;

import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.ApiResponse;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for task management endpoints.
 * <p>
 * All task endpoints are scoped under /api/projects/{id}/tasks.
 * The PATCH status endpoint triggers automatic activity logging via AOP.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "Tasks", description = "Task CRUD endpoints within a project")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "Get tasks by project", description = "Returns a paginated list of tasks, optionally filtered by status")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tasks retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getTasksByProject(projectId, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Get task by ID", description = "Returns a single task within a project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> getTaskById(
            @PathVariable Long projectId, @PathVariable Long taskId) {
        TaskResponse task = taskService.getTaskById(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @PostMapping
    @Operation(summary = "Create a task", description = "Creates a new task within a project")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Task created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the project owner")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @PathVariable Long projectId, @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.createTask(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task", description = "Updates an existing task")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the project owner")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long projectId, @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskRequest request) {
        TaskResponse task = taskService.updateTask(projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status", description = "Updates only the status of a task. This triggers automatic activity logging.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<ApiResponse<TaskResponse>> updateTaskStatus(
            @PathVariable Long projectId, @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskResponse task = taskService.updateTaskStatus(projectId, taskId, request);
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", task));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task", description = "Deletes a task. Only the project owner can delete.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Task deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not the project owner")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(projectId, taskId);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }
}
