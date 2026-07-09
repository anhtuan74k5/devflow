package com.example.devflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateTaskActivityRequest {
    @NotBlank(message = "Content is required")
    private String content;

    private String editorType = "editorjs";
}
