package com.vsk.orbito.workspace.service;

import com.vsk.orbito.entity.User;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.workspace.dto.CreateWorkspaceRequest;
import com.vsk.orbito.workspace.dto.WorkspaceResponse;
import com.vsk.orbito.workspace.entity.Workspace;
import com.vsk.orbito.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(
            CreateWorkspaceRequest request, String ownerEmail) {

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Workspace workspace = Workspace.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(owner)
                .build();

        // owner is automatically a member
        workspace.getMembers().add(owner);
        Workspace saved = workspaceRepository.save(workspace);
        return toResponse(saved);
    }

    @Transactional
    public WorkspaceResponse addMember(
            Long workspaceId, Long userId, String requesterEmail) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workspace not found"));

        // only owner can add members
        if (!workspace.getOwner().getEmail().equals(requesterEmail)) {
            throw new IllegalArgumentException(
                    "Only the workspace owner can add members");
        }

        User newMember = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        workspace.getMembers().add(newMember);
        Workspace saved = workspaceRepository.save(workspace);
        return toResponse(saved);
    }

    public List<WorkspaceResponse> getMyWorkspaces(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return workspaceRepository.findByMemberId(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WorkspaceResponse getById(Long id) {
        return workspaceRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workspace not found"));
    }

    private WorkspaceResponse toResponse(Workspace w) {
        return WorkspaceResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .ownerName(w.getOwner().getName())
                .memberCount(w.getMembers().size())
                .createdAt(w.getCreatedAt())
                .build();
    }
}