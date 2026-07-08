package com.example.devflow.repository;

import com.example.devflow.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByOwnerId(Long ownerId);

    /**
     * Find projects where the given user is either the owner or an assignee of any task.
     */
    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.tasks t WHERE p.owner.id = :userId OR t.assignee.id = :userId")
    Page<Project> findAccessibleProjects(@Param("userId") Long userId, Pageable pageable);
}
