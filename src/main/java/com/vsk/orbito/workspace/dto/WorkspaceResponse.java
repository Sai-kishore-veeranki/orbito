package com.vsk.orbito.workspace.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WorkspaceResponse {
    private Long id;
    private String name;
    private String description;
    private String ownerName;
    private int memberCount;
    private LocalDateTime createdAt;
}