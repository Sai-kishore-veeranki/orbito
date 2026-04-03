package com.vsk.orbito.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProjectRequest {
    @NotBlank(message = "Project name is required")
    private String name;
    private String description;
    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;
}