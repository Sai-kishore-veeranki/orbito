package com.vsk.orbito.task.repository;

import com.vsk.orbito.task.entity.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTaskRepository extends JpaRepository<SubTask, Long> {
    List<SubTask> findByParentTaskId(Long taskId);
}