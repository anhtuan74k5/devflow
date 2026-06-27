package com.example.devflow.controller;

import com.example.devflow.config.TestSecurityConfig;
import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.service.ActivityLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
@Import(TestSecurityConfig.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ActivityLogService activityLogService;

    @Test
    @WithMockUser
    @DisplayName("GET /api/projects/{id}/activities → 200 with paginated logs")
    void getActivities_success() throws Exception {
        ActivityLogResponse log1 = ActivityLogResponse.builder()
                .id(1L)
                .content("Task status updated to DOING")
                .createdAt(LocalDateTime.of(2026, 6, 27, 10, 0))
                .projectId(10L)
                .build();

        ActivityLogResponse log2 = ActivityLogResponse.builder()
                .id(2L)
                .content("Task created")
                .createdAt(LocalDateTime.of(2026, 6, 27, 9, 0))
                .projectId(10L)
                .build();

        PageRequest pageable = PageRequest.of(0, 10);
        when(activityLogService.getLogsByProject(eq(10L), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(log1, log2), pageable, 2));

        mockMvc.perform(get("/api/projects/10/activities")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].content").value("Task status updated to DOING"))
                .andExpect(jsonPath("$.data.content[1].content").value("Task created"))
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }
}
