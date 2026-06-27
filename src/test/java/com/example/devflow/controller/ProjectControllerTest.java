package com.example.devflow.controller;

import com.example.devflow.config.TestSecurityConfig;
import com.example.devflow.dto.request.CreateProjectRequest;
import com.example.devflow.dto.response.ProjectResponse;
import com.example.devflow.exception.AccessDeniedException;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.service.ProjectService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
@Import(TestSecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    private ProjectResponse createProjectResponse() {
        return ProjectResponse.builder()
                .id(1L)
                .name("Test Project")
                .description("Test Description")
                .ownerId(1L)
                .ownerUsername("owner")
                .build();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects → 200 with paginated projects")
    void getAllProjects_success() throws Exception {
        PageRequest pageable = PageRequest.of(0, 10);
        when(projectService.getAllProjects(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(createProjectResponse()), pageable, 1));

        mockMvc.perform(get("/api/projects")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Test Project"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id} → 200 with project")
    void getProjectById_success() throws Exception {
        when(projectService.getProjectById(1L)).thenReturn(createProjectResponse());

        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Test Project"))
                .andExpect(jsonPath("$.data.ownerUsername").value("owner"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id} → 404 when project not found")
    void getProjectById_fail_notFound() throws Exception {
        when(projectService.getProjectById(999L))
                .thenThrow(new ResourceNotFoundException("Project", 999L));

        mockMvc.perform(get("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Project not found with id: 999"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/projects → 201 with created project")
    void createProject_success() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("New Project")
                .description("New Description")
                .build();

        when(projectService.createProject(any(CreateProjectRequest.class)))
                .thenReturn(createProjectResponse());

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project created successfully"))
                .andExpect(jsonPath("$.data.name").value("Test Project"));
    }

    @Test
    @WithMockUser
    @DisplayName("POST /api/projects → 400 when name is blank")
    void createProject_fail_validation() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("")
                .description("Description")
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/projects/{id} → 200 with updated project")
    void updateProject_success() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Updated Project")
                .description("Updated Description")
                .build();

        ProjectResponse updated = ProjectResponse.builder()
                .id(1L)
                .name("Updated Project")
                .description("Updated Description")
                .ownerId(1L)
                .ownerUsername("owner")
                .build();

        when(projectService.updateProject(any(), any(CreateProjectRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project updated successfully"))
                .andExpect(jsonPath("$.data.name").value("Updated Project"));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/projects/{id} → 403 when not owner")
    void updateProject_fail_forbidden() throws Exception {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .name("Hacked")
                .description("Hacked")
                .build();

        when(projectService.updateProject(any(), any(CreateProjectRequest.class)))
                .thenThrow(new AccessDeniedException("You are not the owner of this project"));

        mockMvc.perform(put("/api/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("You are not the owner of this project"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/projects/{id} → 200 when owner deletes")
    void deleteProject_success() throws Exception {
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Project deleted successfully"));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/projects/{id} → 404 when project not found")
    void deleteProject_fail_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Project", 999L))
                .when(projectService).deleteProject(999L);

        mockMvc.perform(delete("/api/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    @DisplayName("DELETE /api/projects/{id} → 403 when not owner")
    void deleteProject_fail_forbidden() throws Exception {
        doThrow(new AccessDeniedException("You are not the owner of this project"))
                .when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
}
