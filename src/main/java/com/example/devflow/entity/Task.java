package com.example.devflow.entity;

import com.example.devflow.model.TaskPriority;
import com.example.devflow.model.TaskStatus;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_tasks_status", columnList = "status"),
    @Index(name = "idx_tasks_project", columnList = "project_id")
})
@Data
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;  

    @Enumerated(EnumType.STRING)
    private TaskStatus status = TaskStatus.TODO; // TODO, IN_PROGRESS, DONE

    @Enumerated(EnumType.STRING)
    private TaskPriority priority = TaskPriority.MEDIUM; // LOW, MEDIUM, HIGH, CRITICAL

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;
}