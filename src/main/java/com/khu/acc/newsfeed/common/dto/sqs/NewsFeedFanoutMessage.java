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
 * 뉴스피드 팬아웃용 SQS 메시지
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsFeedFanoutMessage {
    
    private String messageType;  // "POST_CREATED", "POST_DELETED", "POST_UPDATED"
    private String postId;
    private String authorId;
    private Instant timestamp;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();  // 확장 가능한 추가 데이터
    
    // 정적 팩토리 메서드
    public static NewsFeedFanoutMessage forPostCreated(String postId, String authorId) {
        return NewsFeedFanoutMessage.builder()
                .messageType("POST_CREATED")
                .postId(postId)
                .authorId(authorId)
                .timestamp(Instant.now())
                .build();
    }
    
    public static NewsFeedFanoutMessage forPostDeleted(String postId, String authorId) {
        return NewsFeedFanoutMessage.builder()
                .messageType("POST_DELETED")
                .postId(postId)
                .authorId(authorId)
                .timestamp(Instant.now())
                .build();
    }
    
    public static NewsFeedFanoutMessage forPostUpdated(String postId, String authorId) {
        return NewsFeedFanoutMessage.builder()
                .messageType("POST_UPDATED")
                .postId(postId)
                .authorId(authorId)
                .timestamp(Instant.now())
                .build();
    }
}