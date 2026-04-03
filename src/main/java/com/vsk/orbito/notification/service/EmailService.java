package com.vsk.orbito.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // @Async makes this run in a background thread
    // main thread returns immediately — email sends in background
    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("noreply@orbito.dev");
            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // we log but never throw — email failure
            // should never crash the main flow
        }
    }

    public void sendPRMergedEmail(
            String to, String authorName,
            String prTitle, String mergedBy) {
        String subject = "[Orbito] Your PR was merged — " + prTitle;
        String body = String.format("""
                Hi %s,
                
                Great news! Your pull request "%s" has been merged by %s.
                
                Your linked task has been automatically marked as DONE.
                
                Keep up the great work!
                
                — The Orbito Team
                """, authorName, prTitle, mergedBy);
        sendEmail(to, subject, body);
    }

    public void sendTaskAssignedEmail(
            String to, String assigneeName,
            String taskTitle, String projectName,
            String assignedBy) {
        String subject = "[Orbito] New task assigned — " + taskTitle;
        String body = String.format("""
                Hi %s,
                
                A new task has been assigned to you in project "%s".
                
                Task: %s
                Assigned by: %s
                
                Log in to Orbito to view the full details.
                
                — The Orbito Team
                """, assigneeName, projectName, taskTitle, assignedBy);
        sendEmail(to, subject, body);
    }

    public void sendPRCommentEmail(
            String to, String authorName,
            String prTitle, String commenterName,
            String commentContent) {
        String subject = "[Orbito] New comment on your PR — " + prTitle;
        String body = String.format("""
                Hi %s,
                
                %s commented on your pull request "%s":
                
                "%s"
                
                Log in to Orbito to reply.
                
                — The Orbito Team
                """, authorName, commenterName, prTitle, commentContent);
        sendEmail(to, subject, body);
    }
}