package com.vsk.orbito.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskAssignedEvent {
    private Long taskId;
    private String taskTitle;
    private String assigneeEmail;
    private String assigneeName;
    private String assignedByName;
    private String projectName;
}