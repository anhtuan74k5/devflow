package com.example.devflow.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TaskActivityResponse {
    private Long id;
    private Long taskId;
    private String content;
    private String editorType;
    private LocalDateTime createdAt;
}
