package com.vsk.orbito.project.controller;

import com.vsk.orbito.dto.response.ApiResponse;
import com.vsk.orbito.project.dto.CreateProjectRequest;
import com.vsk.orbito.project.dto.ProjectResponse;
import com.vsk.orbito.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Project", description = "Project management")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> create(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        ProjectResponse response = projectService
                .createProject(request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Project created", response));
    }

    @GetMapping("/workspace/{workspaceId}")
    @Operation(summary = "Get all projects in a workspace")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getByWorkspace(
            @PathVariable Long workspaceId) {

        return ResponseEntity.ok(
                ApiResponse.success("Projects fetched",
                        projectService.getProjectsByWorkspace(workspaceId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ApiResponse<ProjectResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Project fetched",
                        projectService.getById(id)));
    }
}