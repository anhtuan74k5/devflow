package com.example.devflow.service;

import com.example.devflow.dto.request.CreateTaskActivityRequest;
import com.example.devflow.dto.response.TaskActivityResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskActivityService {
    TaskActivityResponse createActivity(Long projectId, Long taskId, CreateTaskActivityRequest request, Long userId);
    Page<TaskActivityResponse> getActivitiesByTaskId(Long projectId, Long taskId, Pageable pageable);
}
