package com.khu.acc.newsfeed.notification;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Notifications")
public class Notification {

    @DynamoDBHashKey(attributeName = "notificationId")
    private String notificationId;

    @DynamoDBAttribute(attributeName = "userId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserNotificationsIndex")
    private String userId; // 알림을 받을 사용자

    @DynamoDBAttribute(attributeName = "fromUserId")
    private String fromUserId; // 알림을 발생시킨 사용자

    @DynamoDBAttribute(attributeName = "type")
    @DynamoDBTypeConvertedEnum
    private NotificationType type;

    @DynamoDBAttribute(attributeName = "referenceId")
    private String referenceId; // 관련된 포스트나 댓글 ID

    @DynamoDBAttribute(attributeName = "message")
    private String message;

    @DynamoDBAttribute(attributeName = "isRead")
    @Builder.Default
    private Boolean isRead = false;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "UserNotificationsIndex")
    private Instant createdAt;

    public enum NotificationType {
        LIKE, COMMENT, FOLLOW, MENTION
    }
}
