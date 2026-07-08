package com.example.devflow.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_logs_project", columnList = "project_id"),
    @Index(name = "idx_logs_created", columnList = "created_at")
})
@Data
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
}