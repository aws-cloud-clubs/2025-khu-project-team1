package com.khu.acc.newsfeed.common.exception.like;

import lombok.Getter;

@Getter
public class LikeAlreadyExistsException extends RuntimeException {

    private final LikeErrorCode errorCode;
    private final String postId;
    private final String userId;

    public LikeAlreadyExistsException(String postId, String userId) {
        super(LikeErrorCode.LIKE_ALREADY_EXISTS.getCodeWithMessage());
        this.errorCode = LikeErrorCode.LIKE_ALREADY_EXISTS;
        this.postId = postId;
        this.userId = userId;
    }

    public LikeAlreadyExistsException(LikeErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.postId = null;
        this.userId = null;
    }

    public LikeAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = LikeErrorCode.LIKE_ALREADY_EXISTS;
        this.postId = null;
        this.userId = null;
    }

    // Static factory methods
    public static LikeAlreadyExistsException of(String postId, String userId) {
        return new LikeAlreadyExistsException(postId, userId);
    }

    public static LikeAlreadyExistsException selfLikeNotAllowed() {
        return new LikeAlreadyExistsException(LikeErrorCode.LIKE_SELF_NOT_ALLOWED);
    }
}