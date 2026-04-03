package com.vsk.orbito.workspace.controller;

import com.vsk.orbito.dto.response.ApiResponse;
import com.vsk.orbito.workspace.dto.CreateWorkspaceRequest;
import com.vsk.orbito.workspace.dto.WorkspaceResponse;
import com.vsk.orbito.workspace.service.WorkspaceService;
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
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Tag(name = "Workspace", description = "Workspace management")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    @Operation(summary = "Create a new workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> create(
            @Valid @RequestBody CreateWorkspaceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkspaceResponse response = workspaceService
                .createWorkspace(request, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Workspace created", response));
    }

    @PostMapping("/{workspaceId}/members/{userId}")
    @Operation(summary = "Add a member to workspace")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> addMember(
            @PathVariable Long workspaceId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        WorkspaceResponse response = workspaceService
                .addMember(workspaceId, userId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Member added", response));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all my workspaces")
    public ResponseEntity<ApiResponse<List<WorkspaceResponse>>> getMyWorkspaces(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<WorkspaceResponse> workspaces = workspaceService
                .getMyWorkspaces(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Workspaces fetched", workspaces));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workspace by ID")
    public ResponseEntity<ApiResponse<WorkspaceResponse>> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Workspace fetched",
                        workspaceService.getById(id)));
    }
}