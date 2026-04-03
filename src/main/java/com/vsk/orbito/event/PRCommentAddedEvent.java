package com.vsk.orbito.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRCommentAddedEvent {
    private Long prId;
    private String prTitle;
    private String commentContent;
    private String commenterName;
    private String prAuthorEmail;
    private Long prAuthorId;
}