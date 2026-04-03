package com.vsk.orbito.pr.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "pr_comments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PRComment {

    @Id
    private String id; // MongoDB uses String IDs

    // which PR this comment belongs to
    @Indexed
    private Long prId;

    // who wrote this comment
    private Long authorId;
    private String authorName;

    private String content;

    // optional — which line of code this comment is on
    private Integer lineNumber;

    // optional — reply to another comment (threading)
    private String parentCommentId;

    // nested replies stored directly in the document
    @Builder.Default
    private List<PRComment> replies = new ArrayList<>();

    @Builder.Default
    private boolean isEdited = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}