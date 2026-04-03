package com.vsk.orbito.pr.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private String id;
    private Long prId;
    private String authorName;
    private String content;
    private Integer lineNumber;
    private String parentCommentId;
    private List<CommentResponse> replies;
    private boolean isEdited;
    private LocalDateTime createdAt;
}