package com.vsk.orbito.task.service;

import com.vsk.orbito.entity.User;
import com.vsk.orbito.enums.Role;
import com.vsk.orbito.event.OrbitoEventPublisher;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.project.entity.Project;
import com.vsk.orbito.project.repository.ProjectRepository;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.task.dto.CreateTaskRequest;
import com.vsk.orbito.task.dto.TaskResponse;
import com.vsk.orbito.task.dto.UpdateTaskRequest;
import com.vsk.orbito.task.entity.Task;
import com.vsk.orbito.task.enums.TaskPriority;
import com.vsk.orbito.task.enums.TaskStatus;
import com.vsk.orbito.task.repository.TaskRepository;
import com.vsk.orbito.workspace.entity.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Unit Tests")
class TaskServiceTest {

    @Mock private TaskRepository taskRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrbitoEventPublisher eventPublisher;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        Workspace workspace = Workspace.builder()
                .id(1L)
                .name("Test Workspace")
                .build();

        testUser = User.builder()
                .id(1L)
                .name("Sai Kishore")
                .email("sai@orbito.com")
                .role(Role.DEVELOPER)
                .provider("LOCAL")
                .isActive(true)
                .build();

        testProject = Project.builder()
                .id(1L)
                .name("Orbito Backend")
                .workspace(workspace)
                .maintainer(testUser)
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Build JWT auth")
                .status(TaskStatus.TODO)
                .priority(TaskPriority.HIGH)
                .project(testProject)
                .createdBy(testUser)
                .version(0L)
                .build();
    }

    // ─── CREATE TASK TESTS ────────────────────────────────────

    @Test
    @DisplayName("createTask — success — returns task response")
    void createTask_success_returnsResponse() {
        // arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Build JWT auth");
        request.setProjectId(1L);
        request.setPriority(TaskPriority.HIGH);

        when(projectRepository.findById(1L))
                .thenReturn(Optional.of(testProject));
        when(userRepository.findByEmail("sai@orbito.com"))
                .thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class)))
                .thenReturn(testTask);

        // act
        TaskResponse response = taskService
                .createTask(request, "sai@orbito.com");

        // assert
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Build JWT auth");
        assertThat(response.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(response.getPriority()).isEqualTo(TaskPriority.HIGH);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    @DisplayName("createTask — project not found — throws exception")
    void createTask_projectNotFound_throwsException() {
        // arrange
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(999L);

        when(projectRepository.findById(999L))
                .thenReturn(Optional.empty());

        // act + assert
        assertThatThrownBy(() ->
                taskService.createTask(request, "sai@orbito.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");

        verify(taskRepository, never()).save(any());
    }

    // ─── STATUS TRANSITION TESTS ──────────────────────────────

    @Test
    @DisplayName("updateTask — TODO to IN_PROGRESS — valid transition")
    void updateTask_todoToInProgress_valid() {
        // arrange
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenReturn(testTask);

        // act — should not throw
        assertThatCode(() ->
                taskService.updateTask(1L, request, "sai@orbito.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("updateTask — TODO to DONE — invalid transition throws")
    void updateTask_todoToDone_invalidTransition_throws() {
        // arrange
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.DONE);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        // act + assert
        assertThatThrownBy(() ->
                taskService.updateTask(1L, request, "sai@orbito.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("updateTask — DONE to any status — blocked")
    void updateTask_doneTask_cannotBeChanged() {
        // arrange
        testTask.setStatus(TaskStatus.DONE);

        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(testTask));

        // act + assert
        assertThatThrownBy(() ->
                taskService.updateTask(1L, request, "sai@orbito.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    @DisplayName("updateTask — task not found — throws exception")
    void updateTask_notFound_throwsException() {
        when(taskRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                taskService.updateTask(999L,
                        new UpdateTaskRequest(), "sai@orbito.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}