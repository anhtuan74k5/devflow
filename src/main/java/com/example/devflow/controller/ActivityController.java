package com.example.devflow.controller;

import com.example.devflow.dto.response.ActivityLogResponse;
import com.example.devflow.dto.response.ApiResponse;
import com.example.devflow.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for activity log endpoints.
 * <p>
 * Activity logs are read-only from the API perspective — they are created
 * automatically by the AOP aspect LoggingAspect when task status changes.
 */
@RestController
@RequestMapping("/api/projects/{projectId}/activities")
@Tag(name = "Activities", description = "Activity log endpoints (read-only)")
public class ActivityController {

    private final ActivityLogService activityLogService;

    public ActivityController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    @Operation(summary = "Get activity logs", description = "Returns a paginated list of activity logs for a project, sorted by creation time descending")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Activities retrieved successfully")
    })
    public ResponseEntity<ApiResponse<Page<ActivityLogResponse>>> getActivities(
            @PathVariable Long projectId,
            @ParameterObject Pageable pageable) {
        Page<ActivityLogResponse> logs = activityLogService.getLogsByProject(projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
