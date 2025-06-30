package com.khu.acc.newsfeed.common.exception.comment;

import lombok.Getter;

@Getter
public class CommentLikeOperationException extends RuntimeException {

    private final CommentErrorCode errorCode;
    private final String details;

    public CommentLikeOperationException(CommentErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public CommentLikeOperationException(CommentErrorCode errorCode, String details) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public CommentLikeOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = CommentErrorCode.COMMENT_LIKE_CREATION_FAILED;
        this.details = null;
    }

    // Static factory methods for common scenarios
    public static CommentLikeOperationException creationFailed() {
        return new CommentLikeOperationException(CommentErrorCode.COMMENT_LIKE_CREATION_FAILED);
    }

    public static CommentLikeOperationException deletionFailed() {
        return new CommentLikeOperationException(CommentErrorCode.COMMENT_LIKE_DELETION_FAILED);
    }

    public static CommentLikeOperationException notExists() {
        return new CommentLikeOperationException(CommentErrorCode.COMMENT_LIKE_NOT_EXISTS);
    }

    public static CommentLikeOperationException notFound() {
        return new CommentLikeOperationException(CommentErrorCode.COMMENT_LIKE_NOT_FOUND);
    }
}