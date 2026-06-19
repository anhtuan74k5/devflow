package com.example.devflow.service.impl;

import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.entity.Project;
import com.example.devflow.entity.Task;
import com.example.devflow.entity.User;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.Role;
import com.example.devflow.model.TaskPriority;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.repository.ProjectRepository;
import com.example.devflow.repository.TaskRepository;
import com.example.devflow.repository.UserRepository;
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
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User createOwner() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setRole(Role.ROLE_USER);
        return owner;
    }

    private User createOtherUser() {
        User other = new User();
        other.setId(2L);
        other.setUsername("otheruser");
        other.setRole(Role.ROLE_USER);
        return other;
    }

    private Project createProject(User owner) {
        Project project = new Project();
        project.setId(10L);
        project.setName("Test Project");
        project.setDescription("Test Description");
        project.setOwner(owner);
        return project;
    }

    private Task createTask(Project project, User assignee) {
        Task task = new Task();
        task.setId(100L);
        task.setTitle("Test Task");
        task.setDescription("Task Description");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        task.setProject(project);
        task.setAssignee(assignee);
        return task;
    }

    @Test
    @DisplayName("createTask: owner creates task → returns TaskResponse with correct data")
    void createTask_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("New Task Description")
                .priority(TaskPriority.HIGH)
                .assigneeId(1L)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Task savedTask = createTask(project, owner);
        savedTask.setTitle("New Task");
        savedTask.setDescription("New Task Description");
        savedTask.setPriority(TaskPriority.HIGH);
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponse response = taskService.createTask(10L, request);

        assertNotNull(response);
        assertEquals("New Task", response.getTitle());
        assertEquals("New Task Description", response.getDescription());
        assertEquals(TaskPriority.HIGH, response.getPriority());
        assertEquals(10L, response.getProjectId());
        assertEquals(1L, response.getAssigneeId());
        assertEquals("owner", response.getAssigneeUsername());

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(userRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask: non-owner tries to create task → throws AccessDeniedException")
    void createTask_fail_notOwner() {
        User owner = createOwner();
        User otherUser = createOtherUser();
        Project project = createProject(owner);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Description")
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.createTask(10L, request));

        assertEquals("You are not the owner of this project", exception.getMessage());

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus: owner updates status → returns TaskResponse with new status")
    void updateTaskStatus_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, null);
        task.setStatus(TaskStatus.TODO);

        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.IN_PROGRESS)
                .build();

        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(owner);

        Task updatedTask = createTask(project, null);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse response = taskService.updateTaskStatus(10L, 100L, request);

        assertNotNull(response);
        assertEquals(TaskStatus.IN_PROGRESS, response.getStatus());
        assertEquals(100L, response.getId());

        verify(taskRepository).findByIdAndProjectId(100L, 10L);
        verify(authService).getCurrentUser();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTaskStatus: non-owner tries to update status → throws AccessDeniedException")
    void updateTaskStatus_fail_notOwner() {
        User owner = createOwner();
        User otherUser = createOtherUser();
        Project project = createProject(owner);
        Task task = createTask(project, null);

        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.DONE)
                .build();

        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.updateTaskStatus(10L, 100L, request));

        assertEquals("You are not the owner of this project", exception.getMessage());

        verify(taskRepository).findByIdAndProjectId(100L, 10L);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("getTaskById: task not found → throws ResourceNotFoundException")
    void getTaskById_fail_notFound() {
        when(taskRepository.findByIdAndProjectId(999L, 10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(10L, 999L));

        assertTrue(exception.getMessage().contains("Task"));
        assertTrue(exception.getMessage().contains("999"));

        verify(taskRepository).findByIdAndProjectId(999L, 10L);
    }
}
