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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User createCurrentUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("currentuser");
        user.setRole(Role.ROLE_USER);
        return user;
    }

    private User createOtherUser() {
        User user = new User();
        user.setId(2L);
        user.setUsername("otheruser");
        user.setRole(Role.ROLE_USER);
        return user;
    }

    private Project createProject(Long id, String name, User owner) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription("Description of " + name);
        project.setOwner(owner);
        return project;
    }

    @Test
    @DisplayName("createProject: valid request → returns ProjectResponse with correct data")
    void createProject_success() {
        User currentUser = createCurrentUser();
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("My Project")
                .description("My Project Description")
                .build();

        when(authService.getCurrentUser()).thenReturn(currentUser);

        Project savedProject = createProject(1L, "My Project", currentUser);
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        ProjectResponse response = projectService.createProject(request);

        assertAll("project response",
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals("My Project", response.getName()),
                () -> assertEquals("Description of My Project", response.getDescription()),
                () -> assertEquals(1L, response.getOwnerId()),
                () -> assertEquals("currentuser", response.getOwnerUsername())
        );

        verify(authService).getCurrentUser();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("updateProject: non-owner tries to update → throws AccessDeniedException")
    void updateProject_fail_notOwner() {
        User owner = createCurrentUser();
        User otherUser = createOtherUser();
        Project project = createProject(1L, "Original Project", owner);

        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Hacked Name")
                .description("Hacked Description")
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> projectService.updateProject(1L, request));

        assertEquals("You are not the owner of this project", exception.getMessage());

        verify(projectRepository).findById(1L);
        verify(authService).getCurrentUser();
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    @DisplayName("deleteProject: project not found → throws ResourceNotFoundException")
    void deleteProject_fail_notFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> projectService.deleteProject(999L));

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains("999"));

        verify(projectRepository).findById(999L);
        verifyNoInteractions(authService);
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("updateProject: admin can update any project → returns ProjectResponse")
    void updateProject_admin_success() {
        User owner = createCurrentUser();
        User admin = new User();
        admin.setId(99L);
        admin.setUsername("admin");
        admin.setRole(Role.ROLE_ADMIN);
        Project project = createProject(1L, "Original Project", owner);

        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Admin Updated")
                .description("Admin Updated Description")
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(admin);

        Project updatedProject = createProject(1L, "Admin Updated", owner);
        updatedProject.setDescription("Admin Updated Description");
        when(projectRepository.save(any(Project.class))).thenReturn(updatedProject);

        ProjectResponse response = projectService.updateProject(1L, request);

        assertAll("admin update",
                () -> assertEquals("Admin Updated", response.getName()),
                () -> assertEquals("Admin Updated Description", response.getDescription())
        );

        verify(projectRepository).findById(1L);
        verify(authService).getCurrentUser();
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    @DisplayName("deleteProject: admin can delete any project → success")
    void deleteProject_admin_success() {
        User owner = createCurrentUser();
        User admin = new User();
        admin.setId(99L);
        admin.setUsername("admin");
        admin.setRole(Role.ROLE_ADMIN);
        Project project = createProject(1L, "Original Project", owner);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(admin);

        projectService.deleteProject(1L);

        verify(projectRepository).findById(1L);
        verify(authService).getCurrentUser();
        verify(projectRepository).delete(project);
    }
}
