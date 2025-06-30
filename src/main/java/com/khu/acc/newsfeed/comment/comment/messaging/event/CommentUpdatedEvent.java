package com.khu.acc.newsfeed.comment.comment.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
public class CommentUpdatedEvent {

    private final CommentEventType eventType = CommentEventType.POST_UPDATED;
    private final String commentId;
    private final String postId;
    private final String userId;
    private final Instant updatedAt;

    public CommentUpdatedEvent(String commentId,String postId, String userId, Instant updatedAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.updatedAt = updatedAt;
    }
}