package com.vsk.orbito.project.service;

import com.vsk.orbito.entity.User;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.project.dto.CreateProjectRequest;
import com.vsk.orbito.project.dto.ProjectResponse;
import com.vsk.orbito.project.entity.Project;
import com.vsk.orbito.project.repository.ProjectRepository;
import com.vsk.orbito.repository.UserRepository;
import com.vsk.orbito.workspace.entity.Workspace;
import com.vsk.orbito.workspace.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(
            CreateProjectRequest request, String maintainerEmail) {

        Workspace workspace = workspaceRepository
                .findById(request.getWorkspaceId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Workspace not found"));

        User maintainer = userRepository.findByEmail(maintainerEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .workspace(workspace)
                .maintainer(maintainer)
                .build();

        Project saved = projectRepository.save(project);
        return toResponse(saved);
    }

    public List<ProjectResponse> getProjectsByWorkspace(Long workspaceId) {
        return projectRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getById(Long id) {
        return projectRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found"));
    }

    private ProjectResponse toResponse(Project p) {
        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .workspaceName(p.getWorkspace().getName())
                .maintainerName(p.getMaintainer().getName())
                .createdAt(p.getCreatedAt())
                .build();
    }
}