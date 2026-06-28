package com.example.devflow.service.impl;

import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.entity.ActivityLog;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.repository.ActivityLogRepository;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.service.ActivityLogService;
import com.example.devflow.service.AuthService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of ActivityLogService.
 * <p>
 * This service is intentionally ignorant of Task, User, and other domain entities.
 * It only knows about content strings and project IDs, making it reusable
 * and decoupled from the rest of the system.
 * <p>
 * Access control for viewing logs: ADMIN can view any project's logs,
 * regular users can only view logs of projects they own.
 */
@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  ProjectRepository projectRepository,
                                  AuthService authService) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
        this.authService = authService;
    }

    @Override
    public ActivityLogResponse createLog(String content, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        ActivityLog log = new ActivityLog();
        log.setContent(content);
        log.setCreatedAt(LocalDateTime.now());
        log.setProject(project);

        ActivityLog saved = activityLogRepository.save(log);
        return toResponse(saved);
    }

    @Override
    public Page<ActivityLogResponse> getLogsByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        checkProjectAccess(project);

        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    /**
     * Checks if the current user can view activity logs for the project.
     * ADMIN can view any project's logs. Regular users can only view logs
     * of projects they own.
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

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .content(log.getContent())
                .createdAt(log.getCreatedAt())
                .projectId(log.getProject().getId())
                .build();
    }
}
