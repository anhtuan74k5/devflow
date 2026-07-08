package com.example.devflow.service;

import com.example.devflow.dto.response.ActivityLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for activity log operations.
 * <p>
 * ActivityLogService has no knowledge of Task, Project, or any other entity.
 * It only receives a content string and a projectId — following the principle
 * of least knowledge. The actual logging is triggered automatically by AOP.
 */
public interface ActivityLogService {

    /**
     * Creates a new activity log entry.
     *
     * @param content   a human-readable description of the activity
     * @param projectId the project this activity belongs to
     * @return the created activity log response
     */
    ActivityLogResponse createLog(String content, Long projectId);

    /**
     * Retrieves activity logs for a project, sorted by creation time descending.
     *
     * @param projectId the project ID
     * @param pageable  pagination parameters
     * @return a page of activity log responses
     */
    Page<ActivityLogResponse> getLogsByProject(Long projectId, Pageable pageable);
}
