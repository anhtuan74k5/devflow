package com.example.devflow.service.impl;

import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.entity.ActivityLog;
import com.example.devflow.entity.Project;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.repository.ActivityLogRepository;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.service.ActivityLogService;
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
 */
@Service
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;

    public ActivityLogServiceImpl(ActivityLogRepository activityLogRepository,
                                  ProjectRepository projectRepository) {
        this.activityLogRepository = activityLogRepository;
        this.projectRepository = projectRepository;
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
        return activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(this::toResponse);
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
