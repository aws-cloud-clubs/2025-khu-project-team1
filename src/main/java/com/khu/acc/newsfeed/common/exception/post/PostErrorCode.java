package com.khu.acc.newsfeed.common.exception.post;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Post Aggregate 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements ErrorCode {
    
    // Domain Layer Errors (P001-P099)
    POST_CONTENT_EMPTY("P001", "Post content cannot be empty"),
    POST_CONTENT_TOO_LONG("P002", "Post content exceeds maximum length of 2000 characters"),
    POST_TOO_MANY_IMAGES("P003", "Maximum 10 images allowed per post"),
    POST_INVALID_IMAGE_URL("P004", "Invalid image URL format"),
    POST_LOCATION_TOO_LONG("P005", "Location exceeds maximum length of 100 characters"),
    POST_INVALID_TAG_FORMAT("P006", "Invalid tag format"),
    
    // Application Layer Errors (P100-P399)
    POST_NOT_FOUND("P100", "Post not found"),
    POST_DELETED("P101", "Post has been deleted"),
    POST_INACTIVE("P102", "Post is not active"),
    POST_ACCESS_DENIED("P103", "You are not authorized to access this post"),
    POST_UPDATE_ACCESS_DENIED("P104", "You are not authorized to update this post"),
    POST_DELETE_ACCESS_DENIED("P105", "You are not authorized to delete this post"),
    POST_OWNER_MISMATCH("P106", "Post does not belong to the specified user"),
    
    // Operation Errors (P400-P499)
    POST_CREATION_FAILED("P400", "Failed to create post"),
    POST_UPDATE_FAILED("P401", "Failed to update post"),
    POST_DELETE_FAILED("P402", "Failed to delete post"),
    POST_ALREADY_DELETED("P403", "Post is already deleted"),
    
    // Search and Query Errors (P500-P599)
    POST_SEARCH_QUERY_EMPTY("P500", "Search query cannot be empty"),
    POST_SEARCH_QUERY_TOO_SHORT("P501", "Search query must be at least 2 characters"),
    POST_TAG_NOT_FOUND("P502", "No posts found with the specified tag"),
    POST_INVALID_CURSOR("P503", "Invalid pagination cursor"),
    POST_INVALID_LIMIT("P504", "Invalid pagination limit");
    
    private final String code;
    private final String message;
}