package com.khu.acc.newsfeed.comment.comment.interfaces.dto;

import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
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
    private List<CommentResponse> replies;
    private Long totalReplyCount;
    private Boolean hasMoreReplies;
    private Boolean isLikedByCurrentUser;

    public static CommentResponse from(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.commentId = comment.getCommentId();
        response.postId = comment.getPostId();
        response.userId = comment.getUserId();
        response.content = comment.getContent();
        response.parentCommentId = comment.getParentCommentId();
        response.likesCount = comment.getLikesCount();
        response.isActive = comment.getIsActive();
        response.createdAt = comment.getCreatedAt();
        response.updatedAt = comment.getUpdatedAt();
        response.isReply = comment.isReply();
        return response;
    }
    
    /**
     * 작성자 정보와 함께 CommentResponse 생성
     */
    public static CommentResponse withAuthor(Comment comment, UserResponse author) {
        CommentResponse response = from(comment);
        response.author = author;
        return response;
    }
    
    public static CommentResponse deletedComment(String commentId) {
        CommentResponse response = new CommentResponse();
        response.commentId = commentId;
        response.content = "삭제된 댓글입니다.";
        response.isActive = false;
        return response;
    }
}
