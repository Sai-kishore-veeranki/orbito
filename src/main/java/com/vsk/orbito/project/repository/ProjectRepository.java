package com.vsk.orbito.project.repository;

import com.vsk.orbito.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByWorkspaceId(Long workspaceId);
    List<Project> findByMaintainerId(Long maintainerId);
}