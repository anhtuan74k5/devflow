package com.example.devflow.service.impl;

import com.example.devflow.dto.request.CreateTaskActivityRequest;
import com.example.devflow.dto.response.TaskActivityResponse;
import com.example.devflow.entity.Task;
import com.example.devflow.entity.TaskActivity;
import com.example.devflow.exception.ResourceNotFoundException;
import com.example.devflow.model.TaskStatus;
import com.example.devflow.repository.TaskActivityRepository;
import com.example.devflow.repository.TaskRepository;
import com.example.devflow.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskActivityServiceImpl implements TaskActivityService {

    private final TaskActivityRepository taskActivityRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public TaskActivityResponse createActivity(Long projectId, Long taskId, CreateTaskActivityRequest request, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task does not belong to this project");
        }

        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setContent(request.getContent());
        activity.setEditorType(request.getEditorType() != null ? request.getEditorType() : "editorjs");

        TaskActivity saved = taskActivityRepository.save(activity);

        // Auto change status to IN_PROGRESS when activity is created
        if (task.getStatus() == TaskStatus.TODO) {
            task.setStatus(TaskStatus.IN_PROGRESS);
            taskRepository.save(task);
        }

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskActivityResponse> getActivitiesByTaskId(Long projectId, Long taskId, Pageable pageable) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!task.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("Task does not belong to this project");
        }

        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId, pageable)
                .map(this::toResponse);
    }

    private TaskActivityResponse toResponse(TaskActivity activity) {
        return TaskActivityResponse.builder()
                .id(activity.getId())
                .taskId(activity.getTask().getId())
                .content(activity.getContent())
                .editorType(activity.getEditorType())
                .createdAt(activity.getCreatedAt())
                .build();
    }
}
