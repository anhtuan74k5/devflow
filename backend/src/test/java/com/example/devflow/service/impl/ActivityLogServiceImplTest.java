package com.example.devflow.service.impl;

import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.entity.ActivityLog;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.repository.ActivityLogRepository;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityLogServiceImplTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ActivityLogServiceImpl activityLogService;

    private User createOwner() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setRole(Role.ROLE_USER);
        return owner;
    }

    private User createAdmin() {
        User admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setRole(Role.ROLE_ADMIN);
        return admin;
    }

    private Project createProject(User owner) {
        Project project = new Project();
        project.setId(10L);
        project.setName("Test Project");
        project.setOwner(owner);
        return project;
    }

    private ActivityLog createLog(Project project) {
        ActivityLog log = new ActivityLog();
        log.setId(1L);
        log.setContent("Task status updated to DOING");
        log.setCreatedAt(LocalDateTime.of(2026, 6, 27, 10, 0));
        log.setProject(project);
        return log;
    }

    @Test
    @DisplayName("createLog: valid project → returns ActivityLogResponse")
    void createLog_success() {
        Project project = createProject(createOwner());
        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));

        ActivityLog savedLog = createLog(project);
        when(activityLogRepository.save(any(ActivityLog.class))).thenReturn(savedLog);

        ActivityLogResponse response = activityLogService.createLog(
                "Task status updated to DOING", 10L);

        assertAll("activity log",
                () -> assertNotNull(response),
                () -> assertEquals(1L, response.getId()),
                () -> assertEquals("Task status updated to DOING", response.getContent()),
                () -> assertNotNull(response.getCreatedAt()),
                () -> assertEquals(10L, response.getProjectId())
        );

        verify(projectRepository).findById(10L);
        verify(activityLogRepository).save(any(ActivityLog.class));
    }

    @Test
    @DisplayName("createLog: project not found → throws ResourceNotFoundException")
    void createLog_fail_projectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> activityLogService.createLog("Some content", 999L));

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains("999"));

        verify(projectRepository).findById(999L);
        verifyNoInteractions(activityLogRepository);
    }

    @Test
    @DisplayName("getLogsByProject: owner views logs → returns paginated logs")
    void getLogsByProject_owner_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        ActivityLog log1 = createLog(project);
        ActivityLog log2 = createLog(project);
        log2.setId(2L);
        log2.setContent("Another activity");
        log2.setCreatedAt(LocalDateTime.of(2026, 6, 27, 11, 0));

        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(activityLogRepository.findByProjectIdOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(log2, log1)));

        Page<ActivityLogResponse> result = activityLogService.getLogsByProject(10L, pageable);

        assertAll("paginated logs",
                () -> assertEquals(2, result.getTotalElements()),
                () -> assertEquals("Another activity", result.getContent().get(0).getContent()),
                () -> assertEquals("Task status updated to DOING", result.getContent().get(1).getContent())
        );

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(activityLogRepository).findByProjectIdOrderByCreatedAtDesc(10L, pageable);
    }

    @Test
    @DisplayName("getLogsByProject: admin views any project's logs → returns paginated logs")
    void getLogsByProject_admin_success() {
        User owner = createOwner();
        User admin = createAdmin();
        Project project = createProject(owner);
        ActivityLog log = createLog(project);

        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(admin);
        when(activityLogRepository.findByProjectIdOrderByCreatedAtDesc(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(log)));

        Page<ActivityLogResponse> result = activityLogService.getLogsByProject(10L, pageable);

        assertEquals(1, result.getTotalElements());

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(activityLogRepository).findByProjectIdOrderByCreatedAtDesc(10L, pageable);
    }

    @Test
    @DisplayName("getLogsByProject: non-owner non-admin views logs → throws AccessDeniedException")
    void getLogsByProject_fail_notOwner() {
        User owner = createOwner();
        User otherUser = new User();
        otherUser.setId(3L);
        otherUser.setUsername("other");
        otherUser.setRole(Role.ROLE_USER);
        Project project = createProject(owner);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> activityLogService.getLogsByProject(10L, PageRequest.of(0, 10)));

        assertEquals("You are not the owner of this project", exception.getMessage());

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verifyNoInteractions(activityLogRepository);
    }
}
