package com.khu.acc.newsfeed.common.exception.like;

import lombok.Getter;

@Getter
public class LikeNotFoundException extends RuntimeException {

    private final LikeErrorCode errorCode;
    private final String likeId;

    public LikeNotFoundException(String likeId) {
        super(String.format("%s - Like ID: %s", LikeErrorCode.LIKE_NOT_FOUND.getCodeWithMessage(), likeId));
        this.errorCode = LikeErrorCode.LIKE_NOT_FOUND;
        this.likeId = likeId;
    }

    public LikeNotFoundException(LikeErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.likeId = null;
    }

    public LikeNotFoundException(LikeErrorCode errorCode, String likeId) {
        super(String.format("%s - Like ID: %s", errorCode.getCodeWithMessage(), likeId));
        this.errorCode = errorCode;
        this.likeId = likeId;
    }

    public LikeNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = LikeErrorCode.LIKE_NOT_FOUND;
        this.likeId = null;
    }
}