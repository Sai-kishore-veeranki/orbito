package com.vsk.orbito.scheduler;

import com.vsk.orbito.task.entity.Task;
import com.vsk.orbito.task.repository.TaskRepository;
import com.vsk.orbito.notification.document.Notification;
import com.vsk.orbito.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrbitoScheduler {

    private final TaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    // runs every day at 8 AM
    @Scheduled(cron = "0 0 8 * * *")
    public void sendDailyDigest() {
        log.info("Running daily digest job...");
        // in Week 8 we connect this to email
        // for now it logs — proves @Scheduled works
        List<Task> overdue = taskRepository.findOverdueTasks();
        log.info("Found {} overdue tasks for digest", overdue.size());
    }

    // runs every hour
    @Scheduled(fixedRate = 3600000)
    public void checkOverdueTasks() {
        log.info("Checking overdue tasks...");
        List<Task> overdueTasks = taskRepository.findOverdueTasks();

        overdueTasks.forEach(task -> {
            if (task.getAssignee() != null) {
                Notification notification = Notification.builder()
                        .userId(task.getAssignee().getId())
                        .title("Task Overdue")
                        .message("Task \"" + task.getTitle()
                                + "\" is past its due date")
                        .type("TASK_OVERDUE")
                        .referenceId(task.getId())
                        .build();
                notificationRepository.save(notification);
                log.info("Overdue notification created for task: {}",
                        task.getId());
            }
        });

        log.info("Overdue check complete — {} tasks flagged",
                overdueTasks.size());
    }
}
//```
//
//        ---
//
//        ## Step 16 — Run and test
//
//Run the app. You should see in console:
//        ```
//Tomcat started on port 8080
//Running overdue task check...   ← scheduler fired immediately
//```
//
//        **Test the full event flow in Postman:**
//
//        **1. Create a task with an assignee** — you should receive an email at the assignee's Gmail within 30 seconds.
//
//        **2. Merge a PR** — PR author receives email.
//
//        **3. Add a comment to a PR** — PR author gets notified.
//
//        **4. Check notifications:**
//        ```
//GET /api/notifications
//GET /api/notifications/unread-count
//PATCH /api/notifications/{id}/read
//PATCH /api/notifications/read-all