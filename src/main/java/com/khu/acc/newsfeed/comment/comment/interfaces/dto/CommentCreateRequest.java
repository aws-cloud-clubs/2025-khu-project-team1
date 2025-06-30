package com.khu.acc.newsfeed.comment.comment.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequest {

    @NotBlank(message = "Post ID is required")
    private String postId;

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String content;

    private String parentCommentId; // For nested comments
    
    // Static factory methods
    public static CommentCreateRequest of(String postId, String content) {
        CommentCreateRequest request = new CommentCreateRequest();
        request.postId = postId;
        request.content = content;
        request.parentCommentId = null;
        return request;
    }
    
    public static CommentCreateRequest replyOf(String postId, String content, String parentCommentId) {
        CommentCreateRequest request = new CommentCreateRequest();
        request.postId = postId;
        request.content = content;
        request.parentCommentId = parentCommentId;
        return request;
    }
}