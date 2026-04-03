package com.vsk.orbito.task.repository;

import com.vsk.orbito.task.entity.Task;
import com.vsk.orbito.task.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // get all tasks in a project — paginated
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    // filter by status
    Page<Task> findByProjectIdAndStatus(
            Long projectId, TaskStatus status, Pageable pageable);

    // filter by assignee
    Page<Task> findByProjectIdAndAssigneeId(
            Long projectId, Long assigneeId, Pageable pageable);

    // tasks assigned to a user across all projects
    List<Task> findByAssigneeId(Long assigneeId);

    // count tasks by status for dashboard
    @Query("SELECT t.status, COUNT(t) FROM Task t " +
            "WHERE t.project.id = :projectId GROUP BY t.status")
    List<Object[]> countByStatusForProject(Long projectId);

    // overdue tasks — for @Scheduled job later
    @Query("SELECT t FROM Task t WHERE t.dueDate < CURRENT_DATE " +
            "AND t.status NOT IN ('DONE', 'CANCELLED')")
    List<Task> findOverdueTasks();
}