package com.vsk.orbito.project.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProjectResponse {
    private Long id;
    private String name;
    private String description;
    private String workspaceName;
    private String maintainerName;
    private LocalDateTime createdAt;
}