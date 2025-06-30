package com.khu.acc.newsfeed.comment.commentlike.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
public class CommentLikeCreatedEvent {

    private final CommentLikeEventType eventType = CommentLikeEventType.COMMENT_LIKE_CREATED;
    private final String commentId;
    private final String userId;
    private final String commentLikeId;
    private final Instant timestamp;
    
    public CommentLikeCreatedEvent (String commentId, String userId, String commentLikeId, Instant createdAt) {
        this.commentId = commentId;
        this.userId = userId;
        this.commentLikeId = commentLikeId;
        this.timestamp = createdAt;

    }
}