package com.example.devflow.service.impl;

import com.example.devflow.config.RabbitMQConfig;
import com.example.devflow.dto.ActivityLogMessage;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Implementation of ActivityLogService.
 * <p>
 * This service is intentionally ignorant of Task, User, and other domain entities.
 * It only knows about content strings and project IDs, making it reusable
 * and decoupled from the rest of the system.
 * <p>
 * Access control for viewing logs: ADMIN can view any project's logs,
 * regular users can only view logs of projects they own.
 * <p>
 * createLog() now acts as a PRODUCER: it publishes an ActivityLogMessage to the
 * RabbitMQ "activity-log-exchange" (routing key "activity.log") instead of
 * writing directly to the database. The message stays in the queue until a
 * consumer (to be implemented in a later week) picks it up.
 */
@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;
    private final RabbitTemplate rabbitTemplate;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  ProjectRepository projectRepository,
                                  AuthService authService,
                                  RabbitTemplate rabbitTemplate) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
        this.authService = authService;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public ActivityLogResponse createLog(String content, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        ActivityLogMessage message = new ActivityLogMessage(
                content,
                projectId,
                LocalDateTime.now(ZoneOffset.UTC)
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTIVITY_LOG_EXCHANGE,
                RabbitMQConfig.ACTIVITY_LOG_ROUTING_KEY,
                message
        );

        return toResponse(message);
    }

    /**
     * Builds a response from a published message. Since the message is not yet
     * persisted (no consumer yet), the id is null.
     */
    private ActivityLogResponse toResponse(ActivityLogMessage message) {
        return ActivityLogResponse.builder()
                .content(message.getContent())
                .createdAt(message.getTimestamp())
                .projectId(message.getProjectId())
                .build();
    }

    @Override
    public Page<ActivityLogResponse> getLogsByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        checkProjectAccess(project);

        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<ActivityLogResponse> getAllLogs(Pageable pageable) {
        return activityLogRepository.findAllByOrderByCreatedAtDesc(pageable)
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
