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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
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
    @DisplayName("getTasksByProject: no status filter → returns all tasks")
    void getTasksByProject_noFilter() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, null);
        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectId(10L, pageable))
                .thenReturn(new PageImpl<>(List.of(task)));

        Page<TaskResponse> result = taskService.getTasksByProject(10L, null, pageable);

        assertAll("tasks without filter",
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals("Test Task", result.getContent().get(0).getTitle())
        );

        verify(projectRepository).findById(10L);
        verify(taskRepository).findByProjectId(10L, pageable);
        verify(taskRepository, never()).findByProjectIdAndStatus(any(), any(), any());
    }

    @Test
    @DisplayName("getTasksByProject: with status filter → returns filtered tasks")
    void getTasksByProject_withStatusFilter() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, null);
        task.setStatus(TaskStatus.IN_PROGRESS);
        PageRequest pageable = PageRequest.of(0, 10);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(taskRepository.findByProjectIdAndStatus(10L, TaskStatus.IN_PROGRESS, pageable))
                .thenReturn(new PageImpl<>(List.of(task)));

        Page<TaskResponse> result = taskService.getTasksByProject(10L, TaskStatus.IN_PROGRESS, pageable);

        assertAll("tasks with filter",
                () -> assertEquals(1, result.getTotalElements()),
                () -> assertEquals(TaskStatus.IN_PROGRESS, result.getContent().get(0).getStatus())
        );

        verify(projectRepository).findById(10L);
        verify(taskRepository).findByProjectIdAndStatus(10L, TaskStatus.IN_PROGRESS, pageable);
        verify(taskRepository, never()).findByProjectId(any(), any());
    }

    @Test
    @DisplayName("getTasksByProject: project not found → throws ResourceNotFoundException")
    void getTasksByProject_fail_projectNotFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTasksByProject(999L, null, PageRequest.of(0, 10)));

        assertTrue(exception.getMessage().contains("Project"));
        assertTrue(exception.getMessage().contains("999"));

        verify(projectRepository).findById(999L);
        verifyNoInteractions(taskRepository);
    }

    @Test
    @DisplayName("getTaskById: task exists → returns TaskResponse")
    void getTaskById_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, owner);

        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(10L, 100L);

        assertAll("task response",
                () -> assertEquals(100L, response.getId()),
                () -> assertEquals("Test Task", response.getTitle()),
                () -> assertEquals(1L, response.getAssigneeId()),
                () -> assertEquals("owner", response.getAssigneeUsername())
        );

        verify(taskRepository).findByIdAndProjectId(100L, 10L);
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

        assertAll("created task",
                () -> assertNotNull(response),
                () -> assertEquals("New Task", response.getTitle()),
                () -> assertEquals("New Task Description", response.getDescription()),
                () -> assertEquals(TaskPriority.HIGH, response.getPriority()),
                () -> assertEquals(10L, response.getProjectId()),
                () -> assertEquals(1L, response.getAssigneeId()),
                () -> assertEquals("owner", response.getAssigneeUsername())
        );

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
    @DisplayName("createTask: assignee not found → throws ResourceNotFoundException")
    void createTask_fail_assigneeNotFound() {
        User owner = createOwner();
        Project project = createProject(owner);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Description")
                .assigneeId(999L)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(10L, request));

        assertTrue(exception.getMessage().contains("User"));
        assertTrue(exception.getMessage().contains("999"));

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(userRepository).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask: owner updates task → returns updated TaskResponse")
    void updateTask_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, null);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Updated Title")
                .description("Updated Description")
                .priority(TaskPriority.HIGH)
                .assigneeId(1L)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        Task updatedTask = createTask(project, owner);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setPriority(TaskPriority.HIGH);
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        TaskResponse response = taskService.updateTask(10L, 100L, request);

        assertAll("updated task",
                () -> assertEquals("Updated Title", response.getTitle()),
                () -> assertEquals("Updated Description", response.getDescription()),
                () -> assertEquals(TaskPriority.HIGH, response.getPriority()),
                () -> assertEquals(1L, response.getAssigneeId())
        );

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(taskRepository).findByIdAndProjectId(100L, 10L);
        verify(userRepository).findById(1L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("updateTask: non-owner tries to update → throws AccessDeniedException")
    void updateTask_fail_notOwner() {
        User owner = createOwner();
        User otherUser = createOtherUser();
        Project project = createProject(owner);
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Hacked")
                .description("Hacked")
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.updateTask(10L, 100L, request));

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

        assertAll("updated status",
                () -> assertNotNull(response),
                () -> assertEquals(TaskStatus.IN_PROGRESS, response.getStatus()),
                () -> assertEquals(100L, response.getId())
        );

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
    @DisplayName("deleteTask: owner deletes task → success")
    void deleteTask_success() {
        User owner = createOwner();
        Project project = createProject(owner);
        Task task = createTask(project, null);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndProjectId(100L, 10L)).thenReturn(Optional.of(task));

        taskService.deleteTask(10L, 100L);

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(taskRepository).findByIdAndProjectId(100L, 10L);
        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("deleteTask: non-owner tries to delete → throws AccessDeniedException")
    void deleteTask_fail_notOwner() {
        User owner = createOwner();
        User otherUser = createOtherUser();
        Project project = createProject(owner);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(otherUser);

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskService.deleteTask(10L, 100L));

        assertEquals("You are not the owner of this project", exception.getMessage());

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    @DisplayName("deleteTask: task not found → throws ResourceNotFoundException")
    void deleteTask_fail_taskNotFound() {
        User owner = createOwner();
        Project project = createProject(owner);

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(authService.getCurrentUser()).thenReturn(owner);
        when(taskRepository.findByIdAndProjectId(999L, 10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> taskService.deleteTask(10L, 999L));

        assertTrue(exception.getMessage().contains("Task"));
        assertTrue(exception.getMessage().contains("999"));

        verify(projectRepository).findById(10L);
        verify(authService).getCurrentUser();
        verify(taskRepository).findByIdAndProjectId(999L, 10L);
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
