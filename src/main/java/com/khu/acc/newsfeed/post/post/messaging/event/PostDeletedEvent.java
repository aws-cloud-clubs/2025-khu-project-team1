package com.khu.acc.newsfeed.post.post.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 포스트 삭제 시 발행되는 도메인 이벤트
 */
@Getter
public class PostDeletedEvent {
    
    private final PostEventType eventType = PostEventType.POST_DELETED;
    private final String postId;
    private final String userId;
    private final Instant deletedAt;

    public PostDeletedEvent(String postId, String userId, Instant deletedAt) {
        this.postId = postId;
        this.userId = userId;
        this.deletedAt = deletedAt;
    }
}