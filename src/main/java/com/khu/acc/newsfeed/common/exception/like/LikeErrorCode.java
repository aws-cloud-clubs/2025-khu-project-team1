package com.khu.acc.newsfeed.common.exception.like;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Like Aggregate 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum LikeErrorCode implements ErrorCode {
    
    // Domain Layer Errors (L001-L099)
    LIKE_SELF_NOT_ALLOWED("L001", "Cannot like your own post"),
    COMMENT_LIKE_SELF_NOT_ALLOWED("L002", "Cannot like your own comment"),
    
    // Application Layer Errors (L100-L399)
    LIKE_NOT_FOUND("L100", "Like not found"),
    LIKE_ALREADY_EXISTS("L101", "User has already liked this post"),
    LIKE_NOT_EXISTS("L102", "User has not liked this post"),
    LIKE_POST_NOT_FOUND("L103", "Cannot like non-existent post"),
    LIKE_USER_NOT_FOUND("L104", "User not found for like operation"),
    
    COMMENT_LIKE_NOT_FOUND("L200", "Comment like not found"),
    COMMENT_LIKE_ALREADY_EXISTS("L201", "User has already liked this comment"),
    COMMENT_LIKE_NOT_EXISTS("L202", "User has not liked this comment"),
    COMMENT_LIKE_COMMENT_NOT_FOUND("L203", "Cannot like non-existent comment"),
    
    // Operation Errors (L400-L499)
    LIKE_CREATION_FAILED("L400", "Failed to create like"),
    LIKE_DELETION_FAILED("L401", "Failed to delete like"),
    LIKE_COUNT_SYNC_FAILED("L402", "Failed to synchronize like count"),
    
    COMMENT_LIKE_CREATION_FAILED("L450", "Failed to create comment like"),
    COMMENT_LIKE_DELETION_FAILED("L451", "Failed to delete comment like");
    
    private final String code;
    private final String message;
}