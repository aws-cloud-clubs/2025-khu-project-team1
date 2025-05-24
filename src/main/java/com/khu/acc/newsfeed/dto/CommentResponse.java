package com.khu.acc.newsfeed.dto;

import com.khu.acc.newsfeed.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private String commentId;
    private String postId;
    private String userId;
    private String content;
    private String parentCommentId;
    private Long likesCount;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // 추가 필드
    private UserResponse author;
    private Boolean isReply;

    public static CommentResponse from(Comment comment) {
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPostId())
                .userId(comment.getUserId())
                .content(comment.getContent())
                .parentCommentId(comment.getParentCommentId())
                .likesCount(comment.getLikesCount())
                .isActive(comment.getIsActive())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .isReply(comment.isReply())
                .build();
    }
}
