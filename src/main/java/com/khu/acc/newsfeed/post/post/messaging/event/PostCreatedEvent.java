package com.khu.acc.newsfeed.post.post.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 포스트 생성 시 발행되는 도메인 이벤트
 */
@Getter
public class PostCreatedEvent {
    
    private PostEventType eventType = PostEventType.POST_CREATED;
    private String postId;
    private String userId;
    private Instant createdAt;
    
    /**
     * 이벤트 타입이 없는 생성자 (기존 호환성 유지)
     */
    public PostCreatedEvent(String postId, String userId, Instant createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }
    
    /**
     * Lambda 호환성을 위한 getAuthorId 메서드
     */
    public String getAuthorId() {
        return userId;
    }
}