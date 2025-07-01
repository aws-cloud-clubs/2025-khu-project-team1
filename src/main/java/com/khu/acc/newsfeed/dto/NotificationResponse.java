package com.khu.acc.newsfeed.dto;

import com.khu.acc.newsfeed.model.Notification;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private String fromUserName;
    private Notification.NotificationType type;
    private String referenceId;
    private String message;
    private Boolean isRead;
    private Instant createdAt;

    public enum NotificationType {
        LIKE, COMMENT, FOLLOW, MENTION
    }

    public static NotificationResponse fromNotification(Notification notification, String fromUser) {
        return NotificationResponse.builder()
            .fromUserName(fromUser)
            .type(notification.getType())
            .referenceId(notification.getReferenceId())
            .message(notification.getMessage())
            .isRead(notification.getIsRead())
            .createdAt(notification.getCreatedAt())
            .build();

    }

}
