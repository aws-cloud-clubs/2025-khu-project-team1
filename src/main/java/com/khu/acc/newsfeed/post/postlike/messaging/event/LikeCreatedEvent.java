package com.khu.acc.newsfeed.post.postlike.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
public class LikeCreatedEvent {

    private final PostLikeEventType eventType = PostLikeEventType.POST_LIKE_CREATED;
    private final String postId;
    private final String userId;
    private final String likeId;
    private final Instant timestamp;
    
    public LikeCreatedEvent(String postId, String userId, String likeId, Instant timestamp) {
        this.postId = postId;
        this.userId = userId;
        this.likeId = likeId;
        this.timestamp = timestamp;

    }
}