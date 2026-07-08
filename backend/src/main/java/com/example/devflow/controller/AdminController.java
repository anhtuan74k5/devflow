package com.example.devflow.controller;

import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.dto.response.ApiResponse;
import com.example.devflow.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only REST controller for system-wide operations.
 * <p>
 * All endpoints in this controller require the ADMIN role.
 * Access is enforced by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin-only endpoints")
public class AdminController {

    private final ActivityLogService activityLogService;

    public AdminController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping("/activities")
    @Operation(summary = "Get all system activity logs", description = "Returns a paginated list of ALL activity logs across all projects. Admin only.")
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getAllActivities(
            @ParameterObject Pageable pageable) {
        Page<ActivityLogResponse> logs = activityLogService.getAllLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
