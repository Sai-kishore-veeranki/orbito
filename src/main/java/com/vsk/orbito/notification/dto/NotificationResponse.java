package com.vsk.orbito.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private String title;
    private String message;
    private String type;
    private boolean isRead;
    private Long referenceId;
    private LocalDateTime createdAt;
}