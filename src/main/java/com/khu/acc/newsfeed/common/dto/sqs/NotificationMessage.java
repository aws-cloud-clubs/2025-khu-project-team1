package com.khu.acc.newsfeed.common.dto.sqs;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 알림 서비스용 SQS 메시지
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationMessage {
    
    private String type;         // "NEW_POST", "LIKE", "COMMENT", "FOLLOW", etc.
    private String targetUserId; // 알림을 받을 사용자 (선택적, 팬아웃의 경우 null)
    private String actorUserId;  // 액션을 수행한 사용자
    private String referenceId;  // postId, commentId 등 참조 ID
    private String referenceType; // "POST", "COMMENT", etc.
    private Instant timestamp;
    
    @Builder.Default
    private Map<String, Object> data = new HashMap<>();  // 추가 데이터
    
    // 정적 팩토리 메서드
    public static NotificationMessage forNewPost(String actorUserId, String postId) {
        return NotificationMessage.builder()
                .type("NEW_POST")
                .actorUserId(actorUserId)
                .referenceId(postId)
                .referenceType("POST")
                .timestamp(Instant.now())
                .build();
    }
    
    public static NotificationMessage forPostLike(String actorUserId, String targetUserId, String postId) {
        return NotificationMessage.builder()
                .type("LIKE")
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .referenceId(postId)
                .referenceType("POST")
                .timestamp(Instant.now())
                .build();
    }
    
    public static NotificationMessage forNewComment(String actorUserId, String targetUserId, String commentId, String postId) {
        Map<String, Object> data = new HashMap<>();
        data.put("postId", postId);
        
        return NotificationMessage.builder()
                .type("COMMENT")
                .targetUserId(targetUserId)
                .actorUserId(actorUserId)
                .referenceId(commentId)
                .referenceType("COMMENT")
                .timestamp(Instant.now())
                .data(data)
                .build();
    }
}