package com.khu.acc.newsfeed.common.exception.comment;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Comment Aggregate 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
    
    // Domain Layer Errors (C001-C099)
    COMMENT_CONTENT_EMPTY("C001", "Comment content cannot be empty"),
    COMMENT_CONTENT_TOO_LONG("C002", "Comment content exceeds maximum length of 1000 characters"),
    COMMENT_REPLY_DEPTH_EXCEEDED("C003", "Reply depth exceeded maximum allowed"),
    COMMENT_INVALID_PARENT("C004", "Invalid parent comment"),
    
    // Application Layer Errors (C100-C399)
    COMMENT_NOT_FOUND("C100", "Comment not found"),
    COMMENT_DELETED("C101", "Comment has been deleted"),
    COMMENT_INACTIVE("C102", "Comment is not active"),
    COMMENT_ACCESS_DENIED("C103", "You are not authorized to access this comment"),
    COMMENT_UPDATE_ACCESS_DENIED("C104", "You are not authorized to update this comment"),
    COMMENT_DELETE_ACCESS_DENIED("C105", "You are not authorized to delete this comment"),
    COMMENT_OWNER_MISMATCH("C106", "Comment does not belong to the specified user"),
    PARENT_COMMENT_NOT_FOUND("C107", "Parent comment not found"),
    COMMENT_POST_NOT_FOUND("C108", "Cannot comment on non-existent post"),
    
    // Operation Errors (C400-C499)
    COMMENT_CREATION_FAILED("C400", "Failed to create comment"),
    COMMENT_UPDATE_FAILED("C401", "Failed to update comment"),
    COMMENT_DELETE_FAILED("C402", "Failed to delete comment"),
    COMMENT_ALREADY_DELETED("C403", "Comment is already deleted"),
    COMMENT_COUNT_SYNC_FAILED("C404", "Failed to synchronize comment count"),
    COMMENT_LIKE_ALREADY_EXISTS("C405", "Comment like already exists"),
    COMMENT_LIKE_CREATION_FAILED("C406", "Failed to create comment like"),
    COMMENT_LIKE_DELETION_FAILED("C407", "Failed to delete comment like"),
    COMMENT_LIKE_NOT_EXISTS("C408", "Comment like does not exist"),
    COMMENT_LIKE_NOT_FOUND("C409", "Comment like not found");
    
    private final String code;
    private final String message;
}