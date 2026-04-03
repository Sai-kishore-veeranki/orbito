package com.vsk.orbito.pr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePRRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotBlank(message = "Source branch is required")
    private String sourceBranch;

    @NotBlank(message = "Target branch is required")
    private String targetBranch;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    // optional — link to a task
    private Long linkedTaskId;
}