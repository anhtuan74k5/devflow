package com.example.devflow.service.impl;

import com.example.devflow.dto.request.CreateProjectRequest;
import com.example.devflow.dto.response.ProjectResponse;
import com.example.devflow.dto.response.ProjectStatsResponse;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.repository.TaskRepository;
import com.example.devflow.service.AuthService;
import com.example.devflow.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of ProjectService.
 * <p>
 * Enforces owner-based access control: only the project owner can update or delete.
 * ADMIN users bypass the owner check and can manage any project.
 * Uses AuthService.getCurrentUser() to identify the authenticated user,
 * keeping SecurityContext access centralized in one place.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    public ProjectServiceImpl(ProjectRepository projectRepository,
                              TaskRepository taskRepository,
                              AuthService authService) {
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.authService = authService;
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        User currentUser = authService.getCurrentUser();
        // ADMIN sees all projects; regular users see only projects they own or are assigned to
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            return projectRepository.findAll(pageable)
                    .map(this::toProjectResponse);
        }
        return projectRepository.findAccessibleProjects(currentUser.getId(), pageable)
                .map(this::toProjectResponse);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = findProjectOrThrow(id);
        return toProjectResponse(project);
    }

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        User currentUser = authService.getCurrentUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Override
    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = findProjectOrThrow(id);
        checkProjectAccess(project);

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = findProjectOrThrow(id);
        checkProjectAccess(project);
        projectRepository.delete(project);
    }

    /**
     * Returns task statistics for all projects accessible by the current user.
     * Uses a single aggregate GROUP BY query instead of N+1 individual status queries.
     * <p>
     * Why this approach: The frontend DashboardPage was making 3 API calls per project
     * (TODO, IN_PROGRESS, DONE) resulting in 31 requests for 10 projects.
     * This single query replaces all of them.
     */
    @Override
    public List<ProjectStatsResponse> getProjectStats() {
        User currentUser = authService.getCurrentUser();

        // Get accessible projects
        List<Project> projects;
        if (currentUser.getRole() == Role.ROLE_ADMIN) {
            projects = projectRepository.findAll();
        } else {
            // Use the accessible projects query but without pagination
            Page<Project> projectPage = projectRepository.findAccessibleProjects(
                    currentUser.getId(), Pageable.unpaged());
            projects = projectPage.getContent();
        }

        if (projects.isEmpty()) {
            return List.of();
        }

        // Single aggregate query: returns [projectId, status, count]
        List<Long> projectIds = projects.stream().map(Project::getId).toList();
        List<Object[]> rawCounts = taskRepository.countByProjectIdsGroupByStatus(projectIds);

        // Build a map: projectId -> { status -> count }
        Map<Long, Map<TaskStatus, Long>> countMap = new HashMap<>();
        for (Object[] row : rawCounts) {
            Long pid = (Long) row[0];
            TaskStatus status = (TaskStatus) row[1];
            Long count = (Long) row[2];
            countMap.computeIfAbsent(pid, k -> new HashMap<>()).put(status, count);
        }

        // Build response list
        List<ProjectStatsResponse> stats = new ArrayList<>();
        for (Project project : projects) {
            Map<TaskStatus, Long> statusCounts = countMap.getOrDefault(project.getId(), Map.of());
            long todo = statusCounts.getOrDefault(TaskStatus.TODO, 0L);
            long inProgress = statusCounts.getOrDefault(TaskStatus.IN_PROGRESS, 0L);
            long done = statusCounts.getOrDefault(TaskStatus.DONE, 0L);

            stats.add(ProjectStatsResponse.builder()
                    .projectId(project.getId())
                    .projectName(project.getName())
                    .total(todo + inProgress + done)
                    .todo(todo)
                    .inProgress(inProgress)
                    .done(done)
                    .build());
        }

        return stats;
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /**
     * Checks if the current user is allowed to modify the project.
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

    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .ownerUsername(project.getOwner().getUsername())
                .build();
    }
}

