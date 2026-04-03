package com.vsk.orbito.pr.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCommentRequest {

    @NotBlank(message = "Comment content is required")
    private String content;

    // optional — which line of code
    private Integer lineNumber;

    // optional — reply to existing comment
    private String parentCommentId;
}