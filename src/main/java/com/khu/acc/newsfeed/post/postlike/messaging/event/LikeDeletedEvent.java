package com.khu.acc.newsfeed.post.postlike.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
public class LikeDeletedEvent {

    private final PostLikeEventType eventType = PostLikeEventType.POST_LIKE_DELETED;
    private final String postId;
    private final String userId;
    private final Instant timestamp;
    
    public LikeDeletedEvent(String postId, String userId) {
        this.postId = postId;
        this.userId = userId;
        this.timestamp = Instant.now();

    }
}