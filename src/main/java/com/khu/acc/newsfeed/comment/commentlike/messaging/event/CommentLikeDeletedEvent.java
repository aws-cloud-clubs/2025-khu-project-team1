package com.khu.acc.newsfeed.comment.commentlike.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
public class CommentLikeDeletedEvent {

    private final CommentLikeEventType eventType = CommentLikeEventType.COMMENT_LIKE_DELETED;
    private final String commentId;
    private final String userId;
    private final Instant timestamp;
    
    public CommentLikeDeletedEvent(String commentId, String userId) {
        this.commentId = commentId;
        this.userId = userId;
        this.timestamp = Instant.now();
    }
}