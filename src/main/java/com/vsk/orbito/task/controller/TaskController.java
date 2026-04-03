package com.vsk.orbito.task.controller;

import com.vsk.orbito.dto.response.ApiResponse;
import com.vsk.orbito.task.dto.CreateTaskRequest;
import com.vsk.orbito.task.dto.TaskResponse;
import com.vsk.orbito.task.dto.UpdateTaskRequest;
import com.vsk.orbito.task.enums.TaskStatus;
import com.vsk.orbito.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Task", description = "Task management")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<ApiResponse<TaskResponse>> create(
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TaskResponse response = taskService
                .createTask(request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Task created", response));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        TaskResponse response = taskService
                .updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Task updated", response));
    }

    @GetMapping("/project/{projectId}")
    @Operation(summary = "Get tasks by project — with pagination, filter by status or assignee")
    public ResponseEntity<ApiResponse<Page<TaskResponse>>> getByProject(
            @PathVariable Long projectId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, sortBy));

        Page<TaskResponse> tasks = taskService
                .getTasksByProject(projectId, status, assigneeId, pageable);
        return ResponseEntity.ok(
                ApiResponse.success("Tasks fetched", tasks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Task fetched", taskService.getById(id)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(
                ApiResponse.success("Task deleted", null));
    }

    @GetMapping("/project/{projectId}/stats")
    @Operation(summary = "Get task count by status for a project")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats(
            @PathVariable Long projectId) {
        return ResponseEntity.ok(
                ApiResponse.success("Stats fetched",
                        taskService.getTaskStats(projectId)));
    }
}
//```
//
//        ---
//
//        ## Step 11 — Run and test in Postman
//
//Run the app — Hibernate will auto-create `workspaces`, `workspace_members`, `projects`, `tasks`, `sub_tasks` tables.
//
//        Test this exact flow in Postman — **always add `Authorization: Bearer YOUR_JWT_TOKEN` header**:
//
//        **1. Create Workspace:**
//        ```
//POST http://localhost:8080/api/workspaces
//        {
//        "name": "Orbito Team",
//        "description": "Main development workspace"
//        }
//        ```
//
//        **2. Create Project:**
//        ```
//POST http://localhost:8080/api/projects
//        {
//        "name": "Orbito Backend",
//        "description": "Spring Boot backend development",
//        "workspaceId": 1
//        }
//        ```
//
//        **3. Create Task:**
//        ```
//POST http://localhost:8080/api/tasks
//        {
//        "title": "Implement JWT authentication",
//        "description": "Build secure login with JWT tokens",
//        "projectId": 1,
//        "priority": "HIGH",
//        "dueDate": "2026-04-01"
//        }
//        ```
//
//        **4. Move task to IN_PROGRESS:**
//        ```
//PATCH http://localhost:8080/api/tasks/1
//        {
//        "status": "IN_PROGRESS"
//        }
//        ```
//
//        **5. Try invalid transition (TODO → DONE) — should get 400:**
//        ```
//PATCH http://localhost:8080/api/tasks/1
//        {
//        "status": "DONE"
//        }
//        ```
//
//        **6. Get tasks with pagination:**
//        ```
//GET http://localhost:8080/api/tasks/project/1?page=0&size=5&sortBy=createdAt
//        ```
//
//        **7. Get task stats:**
//        ```
//GET http://localhost:8080/api/tasks/project/1/stats