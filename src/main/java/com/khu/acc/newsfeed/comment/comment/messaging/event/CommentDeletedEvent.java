package com.khu.acc.newsfeed.comment.comment.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
public class CommentDeletedEvent {

    private final CommentEventType eventType = CommentEventType.POST_DELETED;
    private final String commentId;
    private final String postId;
    private final String userId;
    private final Instant deletedAt;

    public CommentDeletedEvent(String commentId,String postId, String userId, Instant deletedAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.deletedAt = deletedAt;
    }
}