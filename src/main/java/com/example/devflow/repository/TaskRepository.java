package com.example.devflow.repository;

import com.example.devflow.entity.Task;
import com.example.devflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Uses @EntityGraph to eagerly fetch the assignee relationship,
     * preventing N+1 queries when listing tasks with their assignee info.
     */
    @EntityGraph(attributePaths = {"assignee"})
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    @EntityGraph(attributePaths = {"assignee"})
    Page<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"assignee"})
    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
