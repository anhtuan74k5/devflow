package com.example.devflow.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Message payload published to the RabbitMQ "activity-log-queue".
 * <p>
 * Represents an activity log event that will be consumed (in a later week)
 * and persisted. For now it is only produced and left in the queue.
 */
public class ActivityLogMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;
    private Long projectId;
    private LocalDateTime timestamp;

    public ActivityLogMessage() {
    }

    public ActivityLogMessage(String content, Long projectId, LocalDateTime timestamp) {
        this.content = content;
        this.projectId = projectId;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
