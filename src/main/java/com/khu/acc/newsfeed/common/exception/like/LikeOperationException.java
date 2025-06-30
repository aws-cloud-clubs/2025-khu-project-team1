package com.khu.acc.newsfeed.common.exception.like;

import lombok.Getter;

@Getter
public class LikeOperationException extends RuntimeException {

    private final LikeErrorCode errorCode;
    private final String details;

    public LikeOperationException(LikeErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public LikeOperationException(LikeErrorCode errorCode, String details) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public LikeOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = LikeErrorCode.LIKE_CREATION_FAILED;
        this.details = null;
    }

    // Static factory methods for common scenarios
    public static LikeOperationException creationFailed() {
        return new LikeOperationException(LikeErrorCode.LIKE_CREATION_FAILED);
    }

    public static LikeOperationException deletionFailed() {
        return new LikeOperationException(LikeErrorCode.LIKE_DELETION_FAILED);
    }

    public static LikeOperationException countSyncFailed() {
        return new LikeOperationException(LikeErrorCode.LIKE_COUNT_SYNC_FAILED);
    }

    public static LikeOperationException notExists() {
        return new LikeOperationException(LikeErrorCode.LIKE_NOT_EXISTS);
    }
}