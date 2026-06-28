package com.example.devflow.service.impl;

import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.Task;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.repository.TaskRepository;
import com.example.devflow.repository.UserRepository;
import com.example.devflow.service.AuthService;
import com.example.devflow.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of TaskService.
 * <p>
 * All task operations verify project ownership via checkProjectAccess().
 * The updateTaskStatus() method is intentionally NOT calling ActivityLogService
 * directly — the AOP aspect LoggingAspect handles that automatically.
 * Uses @EntityGraph in the repository to prevent N+1 queries on assignee.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    public TaskServiceImpl(TaskRepository taskRepository,
                           ProjectRepository projectRepository,
                           UserRepository userRepository,
                           AuthService authService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public Page<TaskResponse> getTasksByProject(Long projectId, TaskStatus status, Pageable pageable) {
        findProjectOrThrow(projectId);

        Page<Task> tasks;
        if (status != null) {
            tasks = taskRepository.findByProjectIdAndStatus(projectId, status, pageable);
        } else {
            tasks = taskRepository.findByProjectId(projectId, pageable);
        }
        return tasks.map(this::toTaskResponse);
    }

    @Override
    public TaskResponse getTaskById(Long projectId, Long taskId) {
        Task task = findTaskOrThrow(projectId, taskId);
        return toTaskResponse(task);
    }

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {
        Project project = findProjectOrThrow(projectId);
        checkProjectAccess(project);

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setProject(project);

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);
        return toTaskResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, CreateTaskRequest request) {
        Project project = findProjectOrThrow(projectId);
        checkProjectAccess(project);

        Task task = findTaskOrThrow(projectId, taskId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());

        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.getAssigneeId()));
            task.setAssignee(assignee);
        } else {
            task.setAssignee(null);
        }

        Task saved = taskRepository.save(task);
        return toTaskResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse updateTaskStatus(Long projectId, Long taskId, UpdateTaskStatusRequest request) {
        Task task = findTaskOrThrow(projectId, taskId);
        Project project = task.getProject();
        checkProjectAccess(project);
        task.setStatus(request.getStatus());
        Task saved = taskRepository.save(task);
        return toTaskResponse(saved);
    }

    @Override
    public void deleteTask(Long projectId, Long taskId) {
        Project project = findProjectOrThrow(projectId);
        checkProjectAccess(project);

        Task task = findTaskOrThrow(projectId, taskId);
        taskRepository.delete(task);
    }

    private Project findProjectOrThrow(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
    }

    private Task findTaskOrThrow(Long projectId, Long taskId) {
        return taskRepository.findByIdAndProjectId(taskId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
    }

    /**
     * Checks if the current user is allowed to modify tasks in the project.
     * Owner is always allowed. ADMIN users bypass the owner check.
     */
    private void checkProjectAccess(Project project) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return;
        }
        if (!project.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not the owner of this project");
        }
    }

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse.TaskResponseBuilder builder = TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .projectId(task.getProject().getId());

        if (task.getAssignee() != null) {
            builder.assigneeId(task.getAssignee().getId())
                    .assigneeUsername(task.getAssignee().getUsername());
        }

        return builder.build();
    }
}
