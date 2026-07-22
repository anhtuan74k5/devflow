package com.example.devflow.repository;

import com.example.devflow.entity.Task;
import com.example.devflow.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Returns task counts grouped by status for a list of project IDs.
     * Used by the dashboard to avoid N+1 queries (one query instead of 3 per project).
     */
    @Query("SELECT t.project.id, t.status, COUNT(t) FROM Task t WHERE t.project.id IN :projectIds GROUP BY t.project.id, t.status")
    List<Object[]> countByProjectIdsGroupByStatus(@Param("projectIds") List<Long> projectIds);

    /**
     * Uses @EntityGraph to eagerly fetch the assignee relationship,
     * preventing N+1 queries when listing tasks with their assignee info.
     * Sorted by priority descending (CRITICAL first, LOW last) then by id descending.
     */
    @EntityGraph(attributePaths = {"assignee"})
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId " +
           "ORDER BY CASE t.priority WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 END, t.id DESC")
    Page<Task> findByProjectId(@Param("projectId") Long projectId, Pageable pageable);

    /**
     * Same as findByProjectId but with status filter.
     */
    @EntityGraph(attributePaths = {"assignee"})
    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND t.status = :status " +
           "ORDER BY CASE t.priority WHEN 'CRITICAL' THEN 0 WHEN 'HIGH' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'LOW' THEN 3 END, t.id DESC")
    Page<Task> findByProjectIdAndStatus(@Param("projectId") Long projectId, @Param("status") TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"assignee"})
    Optional<Task> findByIdAndProjectId(Long id, Long projectId);
}
