package com.vsk.orbito.workspace.repository;

import com.vsk.orbito.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {

    // find all workspaces where user is owner
    List<Workspace> findByOwnerId(Long ownerId);

    // find all workspaces where user is a member
    @Query("SELECT w FROM Workspace w JOIN w.members m WHERE m.id = :userId")
    List<Workspace> findByMemberId(Long userId);
}