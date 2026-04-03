package com.vsk.orbito.task.dto;

import com.vsk.orbito.task.enums.TaskPriority;
import com.vsk.orbito.task.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private String projectName;
    private String assigneeName;
    private String createdByName;
    private LocalDate dueDate;
    private Long version;
    private LocalDateTime createdAt;
}