package com.vsk.orbito.task.service;

import com.vsk.orbito.entity.User;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.project.entity.Project;
import com.vsk.orbito.project.repository.ProjectRepository;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.task.dto.CreateTaskRequest;
import com.vsk.orbito.task.dto.TaskResponse;
import com.vsk.orbito.task.dto.UpdateTaskRequest;
import com.vsk.orbito.task.entity.Task;
import com.vsk.orbito.task.enums.TaskStatus;
import com.vsk.orbito.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public TaskResponse createTask(
            CreateTaskRequest request, String creatorEmail) {

        Project project = projectRepository
                .findById(request.getProjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));

        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Assignee not found"));
        }

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .project(project)
                .createdBy(creator)
                .assignee(assignee)
                .priority(request.getPriority())
                .dueDate(request.getDueDate())
                .build();

        Task saved = taskRepository.save(task);
        log.info("Task created: {} in project: {}",
                saved.getId(), project.getName());
        return toResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(
            Long taskId, UpdateTaskRequest request, String userEmail) {
        try {
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Task not found"));

            // validate status transition
            if (request.getStatus() != null) {
                validateStatusTransition(task.getStatus(), request.getStatus());
                task.setStatus(request.getStatus());
            }

            if (request.getTitle() != null)
                task.setTitle(request.getTitle());
            if (request.getDescription() != null)
                task.setDescription(request.getDescription());
            if (request.getPriority() != null)
                task.setPriority(request.getPriority());
            if (request.getDueDate() != null)
                task.setDueDate(request.getDueDate());

            if (request.getAssigneeId() != null) {
                User assignee = userRepository
                        .findById(request.getAssigneeId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Assignee not found"));
                task.setAssignee(assignee);
            }

            Task saved = taskRepository.save(task);
            return toResponse(saved);

        } catch (ObjectOptimisticLockingFailureException e) {
            // two people tried to update the same task at the same time
            throw new IllegalStateException(
                    "Task was updated by someone else. " +
                            "Please refresh and try again.");
        }
    }

    public Page<TaskResponse> getTasksByProject(
            Long projectId, TaskStatus status,
            Long assigneeId, Pageable pageable) {

        if (status != null) {
            return taskRepository
                    .findByProjectIdAndStatus(projectId, status, pageable)
                    .map(this::toResponse);
        }
        if (assigneeId != null) {
            return taskRepository
                    .findByProjectIdAndAssigneeId(projectId, assigneeId, pageable)
                    .map(this::toResponse);
        }
        return taskRepository
                .findByProjectId(projectId, pageable)
                .map(this::toResponse);
    }

    public TaskResponse getById(Long id) {
        return taskRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));
    }

    @Transactional
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Task not found"));
        taskRepository.delete(task);
    }

    public Map<String, Long> getTaskStats(Long projectId) {
        return taskRepository.countByStatusForProject(projectId)
                .stream()
                .collect(Collectors.toMap(
                        row -> row[0].toString(),
                        row -> (Long) row[1]
                ));
    }

    // state machine — only valid transitions allowed
    private void validateStatusTransition(
            TaskStatus current, TaskStatus next) {
        boolean valid = switch (current) {
            case TODO         -> next == TaskStatus.IN_PROGRESS
                    || next == TaskStatus.CANCELLED;
            case IN_PROGRESS  -> next == TaskStatus.IN_REVIEW
                    || next == TaskStatus.TODO
                    || next == TaskStatus.CANCELLED;
            case IN_REVIEW    -> next == TaskStatus.DONE
                    || next == TaskStatus.IN_PROGRESS;
            case DONE         -> false; // cannot change a DONE task
            case CANCELLED    -> false; // cannot change a CANCELLED task
        };

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    private TaskResponse toResponse(Task t) {
        return TaskResponse.builder()
                .id(t.getId())
                .title(t.getTitle())
                .description(t.getDescription())
                .status(t.getStatus())
                .priority(t.getPriority())
                .projectName(t.getProject().getName())
                .assigneeName(t.getAssignee() != null
                        ? t.getAssignee().getName() : null)
                .createdByName(t.getCreatedBy().getName())
                .dueDate(t.getDueDate())
                .version(t.getVersion())
                .createdAt(t.getCreatedAt())
                .build();
    }
}