package com.vsk.orbito.task.dto;

import com.vsk.orbito.task.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;

    private TaskPriority priority = TaskPriority.MEDIUM;

    private LocalDate dueDate;
}