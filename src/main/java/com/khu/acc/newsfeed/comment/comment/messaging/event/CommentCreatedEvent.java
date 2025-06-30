package com.khu.acc.newsfeed.comment.comment.messaging.event;

import lombok.Getter;

import java.time.Instant;

/**
 * 포스트 생성 시 발행되는 도메인 이벤트
 */
@Getter
public class CommentCreatedEvent {

    private final CommentEventType eventType = CommentEventType.POST_CREATED;
    private final String commentId;
    private final String postId;
    private final String userId;
    private final Instant createdAt;

    /**
     * 이벤트 타입이 없는 생성자 (기존 호환성 유지)
     */
    public CommentCreatedEvent(String commentId, String postId, String userId, Instant createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.createdAt = createdAt;
    }
}