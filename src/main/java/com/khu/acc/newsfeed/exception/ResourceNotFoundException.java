package com.khu.acc.newsfeed.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException user(String userId) {
        return new ResourceNotFoundException("User not found with id: " + userId);
    }

    public static ResourceNotFoundException post(String postId) {
        return new ResourceNotFoundException("Post not found with id: " + postId);
    }

    public static ResourceNotFoundException comment(String commentId) {
        return new ResourceNotFoundException("Comment not found with id: " + commentId);
    }
}