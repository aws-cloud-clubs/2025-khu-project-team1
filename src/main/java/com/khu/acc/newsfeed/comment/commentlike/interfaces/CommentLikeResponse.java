package com.khu.acc.newsfeed.comment.commentlike.interfaces;

import com.khu.acc.newsfeed.comment.commentlike.domain.CommentLike;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentLikeResponse {

    private String commentLikeId;
    private String commentId;
    private String userId;
    private Instant createdAt;

    // 추가 필드
    private UserResponse user;

    public static CommentLikeResponse from(CommentLike commentLike) {
        CommentLikeResponse response = new CommentLikeResponse();
        response.commentLikeId = commentLike.getCommentLikeId();
        response.commentId = commentLike.getCommentId();
        response.userId = commentLike.getUserId();
        response.createdAt = commentLike.getCreatedAt();
        return response;
    }

    public static CommentLikeResponse of(String commentId, String userId) {
        CommentLikeResponse response = new CommentLikeResponse();
        response.commentLikeId = CommentLike.generateCommentLikeId(commentId, userId);
        response.commentId = commentId;
        response.userId = userId;
        response.createdAt = Instant.now();
        return response;
    }
}