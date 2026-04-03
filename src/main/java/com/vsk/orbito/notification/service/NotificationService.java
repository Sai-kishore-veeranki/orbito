package com.vsk.orbito.notification.service;


import com.vsk.orbito.entity.User;
import com.vsk.orbito.event.*;
import com.vsk.orbito.exception.ResourceNotFoundException;
import com.vsk.orbito.notification.document.Notification;
import com.vsk.orbito.notification.dto.NotificationResponse;
import com.vsk.orbito.notification.repository.NotificationRepository;
import com.vsk.orbito.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    // ─── EVENT LISTENERS ──────────────────────────────────────

    @Async
    @EventListener
    public void handlePRMerged(PRMergedEvent event) {
        log.info("Handling PRMergedEvent for PR: {}", event.getPrId());

        // 1. find the PR author's user ID
        User author = userRepository.findByEmail(event.getAuthorEmail())
                .orElse(null);
        if (author == null) return;

        // 2. save in-app notification to MongoDB
        Notification notification = Notification.builder()
                .userId(author.getId())
                .title("Pull Request Merged")
                .message("Your PR \"" + event.getPrTitle()
                        + "\" was merged by " + event.getMergedByName())
                .type("PR_MERGED")
                .referenceId(event.getPrId())
                .build();
        notificationRepository.save(notification);

        // 3. send real-time WebSocket notification to the user
        messagingTemplate.convertAndSendToUser(
                author.getId().toString(),
                "/queue/notifications",
                toResponse(notification)
        );

        // 4. send email in background
        emailService.sendPRMergedEmail(
                event.getAuthorEmail(),
                event.getAuthorName(),
                event.getPrTitle(),
                event.getMergedByName()
        );

        log.info("PRMergedEvent handled for: {}", event.getAuthorEmail());
    }

    @Async
    @EventListener
    public void handleTaskAssigned(TaskAssignedEvent event) {
        log.info("Handling TaskAssignedEvent for task: {}",
                event.getTaskId());

        User assignee = userRepository
                .findByEmail(event.getAssigneeEmail())
                .orElse(null);
        if (assignee == null) return;

        Notification notification = Notification.builder()
                .userId(assignee.getId())
                .title("New Task Assigned")
                .message("Task \"" + event.getTaskTitle()
                        + "\" assigned to you by "
                        + event.getAssignedByName()
                        + " in " + event.getProjectName())
                .type("TASK_ASSIGNED")
                .referenceId(event.getTaskId())
                .build();
        notificationRepository.save(notification);

        // real-time WebSocket alert
        messagingTemplate.convertAndSendToUser(
                assignee.getId().toString(),
                "/queue/notifications",
                toResponse(notification)
        );

        // email in background
        emailService.sendTaskAssignedEmail(
                event.getAssigneeEmail(),
                event.getAssigneeName(),
                event.getTaskTitle(),
                event.getProjectName(),
                event.getAssignedByName()
        );
    }

    @Async
    @EventListener
    public void handlePRCommentAdded(PRCommentAddedEvent event) {
        log.info("Handling PRCommentAddedEvent for PR: {}",
                event.getPrId());

        // notify PR author — but not if they commented on their own PR
        User author = userRepository
                .findByEmail(event.getPrAuthorEmail())
                .orElse(null);
        if (author == null) return;

        Notification notification = Notification.builder()
                .userId(author.getId())
                .title("New Comment on Your PR")
                .message(event.getCommenterName()
                        + " commented on \""
                        + event.getPrTitle() + "\"")
                .type("PR_COMMENT")
                .referenceId(event.getPrId())
                .build();
        notificationRepository.save(notification);

        // real-time WebSocket — PR comment broadcast to all viewers
        messagingTemplate.convertAndSend(
                "/topic/pr/" + event.getPrId() + "/comments",
                event.getCommentContent()
        );

        // real-time personal notification
        messagingTemplate.convertAndSendToUser(
                author.getId().toString(),
                "/queue/notifications",
                toResponse(notification)
        );

        // email in background
        emailService.sendPRCommentEmail(
                event.getPrAuthorEmail(),
                author.getName(),
                event.getPrTitle(),
                event.getCommenterName(),
                event.getCommentContent()
        );
    }

    // ─── NOTIFICATION CRUD ────────────────────────────────────

    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public long getUnreadCount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        return notificationRepository
                .countByUserIdAndIsReadFalse(user.getId());
    }

    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
        List<Notification> unread = notificationRepository
                .findByUserIdAndIsReadFalse(user.getId());
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .title(n.getTitle())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.isRead())
                .referenceId(n.getReferenceId())
                .createdAt(n.getCreatedAt())
                .build();
    }
}