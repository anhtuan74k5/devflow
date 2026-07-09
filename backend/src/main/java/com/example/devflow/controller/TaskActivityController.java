package com.example.devflow.controller;

import com.example.devflow.dto.request.CreateTaskActivityRequest;
import com.example.devflow.dto.response.ApiResponse;
import com.example.devflow.dto.response.TaskActivityResponse;
import com.example.devflow.entity.User;
import com.example.devflow.service.TaskActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks/{taskId}/activities")
@RequiredArgsConstructor
public class TaskActivityController {

    private final TaskActivityService taskActivityService;

    @PostMapping
    public ResponseEntity<ApiResponse<TaskActivityResponse>> createActivity(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody CreateTaskActivityRequest request,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        TaskActivityResponse response = taskActivityService.createActivity(projectId, taskId, request, user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<TaskActivityResponse>>> getActivities(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            Pageable pageable) {
        Page<TaskActivityResponse> activities = taskActivityService.getActivitiesByTaskId(projectId, taskId, pageable);
        return ResponseEntity.ok(ApiResponse.success(activities));
    }
}
