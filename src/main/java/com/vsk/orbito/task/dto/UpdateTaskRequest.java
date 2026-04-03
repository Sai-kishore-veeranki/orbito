package com.vsk.orbito.task.dto;

import com.vsk.orbito.task.enums.TaskPriority;
import com.vsk.orbito.task.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateTaskRequest {
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private Long assigneeId;
    private LocalDate dueDate;
}