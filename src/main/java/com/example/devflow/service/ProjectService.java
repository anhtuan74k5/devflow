package com.example.devflow.service;

import com.example.devflow.dto.request.CreateProjectRequest;
import com.example.devflow.dto.response.ProjectResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for project management operations.
 * <p>
 * Defines CRUD operations for projects with owner-based access control.
 * Every project has an owner who is the only one authorized to modify or delete it.
 */
public interface ProjectService {

    /**
     * Retrieves all projects with pagination.
     *
     * @param pageable pagination parameters
     * @return a page of project responses
     */
    Page<ProjectResponse> getAllProjects(Pageable pageable);

    /**
     * Retrieves a single project by its ID.
     *
     * @param id the project ID
     * @return the project response
     * @throws com.example.devflow.exception.ResourceNotFoundException if project not found
     */
    ProjectResponse getProjectById(Long id);

    /**
     * Creates a new project owned by the currently authenticated user.
     *
     * @param request the project creation details
     * @return the created project response
     */
    ProjectResponse createProject(CreateProjectRequest request);

    /**
     * Updates an existing project. Only the owner can update.
     *
     * @param id      the project ID
     * @param request the updated project details
     * @return the updated project response
     * @throws com.example.devflow.exception.AccessDeniedException if the current user is not the owner
     */
    ProjectResponse updateProject(Long id, CreateProjectRequest request);

    /**
     * Deletes a project. Only the owner can delete.
     *
     * @param id the project ID
     * @throws com.example.devflow.exception.AccessDeniedException if the current user is not the owner
     */
    void deleteProject(Long id);
}
