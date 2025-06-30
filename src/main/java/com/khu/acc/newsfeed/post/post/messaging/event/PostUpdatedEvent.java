package com.khu.acc.newsfeed.post.post.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * 포스트 수정 시 발행되는 도메인 이벤트
 */
@Getter
public class PostUpdatedEvent {
    
    private final PostEventType eventType = PostEventType.POST_UPDATED;
    private final String postId;
    private final String userId;
    private final Instant updatedAt;

    public PostUpdatedEvent(String postId, String userId, Instant updatedAt) {
        this.postId = postId;
        this.userId = userId;
        this.updatedAt = updatedAt;
    }
}