package com.vsk.orbito.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PRMergedEvent {
    private Long prId;
    private String prTitle;
    private String authorEmail;
    private String authorName;
    private String mergedByName;
    private String projectName;
    private Long linkedTaskId;
}