package com.example.devflow.service.impl;

import com.example.devflow.dto.request.CreateProjectRequest;
import com.example.devflow.dto.response.ProjectResponse;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.service.AuthService;
import com.example.devflow.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Implementation of ProjectService.
 * <p>
 * Enforces owner-based access control: only the project owner can update or delete.
 * ADMIN users bypass the owner check and can manage any project.
 * Uses AuthService.getCurrentUser() to identify the authenticated user,
 * keeping SecurityContext access centralized in one place.
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final AuthService authService;

    public ProjectServiceImpl(ProjectRepository projectRepository, AuthService authService) {
        this.projectRepository = projectRepository;
        this.authService = authService;
    }

    @Override
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable)
                .map(this::toProjectResponse);
    }

    @Override
    public ProjectResponse getProjectById(Long id) {
        Project project = findProjectOrThrow(id);
        return toProjectResponse(project);
    }

    @Override
    public ProjectResponse createProject(CreateProjectRequest request) {
        User currentUser = authService.getCurrentUser();

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwner(currentUser);

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Override
    public ProjectResponse updateProject(Long id, CreateProjectRequest request) {
        Project project = findProjectOrThrow(id);
        checkProjectAccess(project);

        project.setName(request.getName());
        project.setDescription(request.getDescription());

        Project saved = projectRepository.save(project);
        return toProjectResponse(saved);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = findProjectOrThrow(id);
        checkProjectAccess(project);
        projectRepository.delete(project);
    }

    private Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project", id));
    }

    /**
     * Checks if the current user is allowed to modify the project.
     * Owner is always allowed. ADMIN users bypass the owner check.
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

    private ProjectResponse toProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .ownerId(project.getOwner().getId())
                .ownerUsername(project.getOwner().getUsername())
                .build();
    }
}
