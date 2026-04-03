package com.vsk.orbito.notification.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    private String id;

    @Indexed
    private Long userId;

    private String title;
    private String message;
    private String type; // PR_MERGED, TASK_ASSIGNED, PR_COMMENT

    @Builder.Default
    private boolean isRead = false;

    private Long referenceId; // prId or taskId

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}