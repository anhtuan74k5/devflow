package com.example.devflow.controller;

import com.example.devflow.config.TestSecurityConfig;
import com.example.devflow.dto.request.CreateTaskRequest;
import com.example.devflow.dto.request.UpdateTaskStatusRequest;
import com.example.devflow.dto.response.TaskResponse;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.TaskPriority;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(TestSecurityConfig.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse createTaskResponse() {
        return TaskResponse.builder()
                .id(100L)
                .title("Test Task")
                .description("Task Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .projectId(10L)
                .assigneeId(1L)
                .assigneeUsername("assignee")
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id}/tasks → 200 with paginated tasks")
    void getTasks_success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        when(taskService.getTasksByProject(eq(10L), isNull(), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(createTaskResponse()), pageable, 1));

        mockMvc.perform(get("/api/projects/10/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Test Task"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id}/tasks?status=TODO → 200 with filtered tasks")
    void getTasks_withStatusFilter() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        when(taskService.getTasksByProject(eq(10L), eq(TaskStatus.TODO), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(createTaskResponse()), pageable, 1));

        mockMvc.perform(get("/api/projects/10/tasks")
                        .param("status", "TODO")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].status").value("TODO"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id}/tasks/{taskId} → 200 with task")
    void getTaskById_success() throws Exception {
        when(taskService.getTaskById(10L, 100L)).thenReturn(createTaskResponse());

        mockMvc.perform(get("/api/projects/10/tasks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Task"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id}/tasks/{taskId} → 404 when task not found")
    void getTaskById_fail_notFound() throws Exception {
        when(taskService.getTaskById(10L, 999L))
                .thenThrow(new ResourceNotFoundException("Task", 999L));

        mockMvc.perform(get("/api/projects/10/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/projects/{id}/tasks → 201 with created task")
    void createTask_success() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("New Description")
                .priority(TaskPriority.HIGH)
                .assigneeId(1L)
                .build();

        when(taskService.createTask(any(), any(CreateTaskRequest.class)))
                .thenReturn(createTaskResponse());

        mockMvc.perform(post("/api/projects/10/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task created successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/projects/{id}/tasks → 400 when title is blank")
    void createTask_fail_validation() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/projects/10/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/projects/{id}/tasks → 403 when not owner")
    void createTask_fail_forbidden() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("New Task")
                .description("Description")
                .build();

        when(taskService.createTask(any(), any(CreateTaskRequest.class)))
                .thenThrow(new AccessDeniedException("You are not the owner of this project"));

        mockMvc.perform(post("/api/projects/10/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/projects/{id}/tasks/{taskId} → 200 with updated task")
    void updateTask_success() throws Exception {
        CreateTaskRequest request = CreateTaskRequest.builder()
                .title("Updated Task")
                .description("Updated Description")
                .build();

        TaskResponse updated = TaskResponse.builder()
                .id(100L)
                .title("Updated Task")
                .description("Updated Description")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.MEDIUM)
                .projectId(10L)
                .build();

        when(taskService.updateTask(any(), any(), any(CreateTaskRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/projects/10/tasks/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task updated successfully"))
                .andExpect(jsonPath("$.data.title").value("Updated Task"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/projects/{id}/tasks/{taskId}/status → 200 with new status")
    void updateTaskStatus_success() throws Exception {
        UpdateTaskStatusRequest request = UpdateTaskStatusRequest.builder()
                .status(TaskStatus.DONE)
                .build();

        TaskResponse updated = TaskResponse.builder()
                .id(100L)
                .title("Test Task")
                .description("Description")
                .status(TaskStatus.DONE)
                .priority(TaskPriority.MEDIUM)
                .projectId(10L)
                .build();

        when(taskService.updateTaskStatus(any(), any(), any(UpdateTaskStatusRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(patch("/api/projects/10/tasks/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("DONE"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/projects/{id}/tasks/{taskId} → 200 when owner deletes")
    void deleteTask_success() throws Exception {
        mockMvc.perform(delete("/api/projects/10/tasks/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Task deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/projects/{id}/tasks/{taskId} → 403 when not owner")
    void deleteTask_fail_forbidden() throws Exception {
        doThrow(new AccessDeniedException("You are not the owner of this project"))
                .when(taskService).deleteTask(10L, 100L);

        mockMvc.perform(delete("/api/projects/10/tasks/100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
