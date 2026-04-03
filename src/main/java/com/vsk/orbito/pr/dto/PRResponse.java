package com.vsk.orbito.pr.dto;

import com.vsk.orbito.pr.enums.PRStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
public class PRResponse {
    private Long id;
    private String title;
    private String description;
    private String sourceBranch;
    private String targetBranch;
    private PRStatus status;
    private String authorName;
    private String projectName;
    private String linkedTaskTitle;
    private Set<String> reviewerNames;
    private Set<String> approvalNames;
    private int commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime mergedAt;
}