package com.khu.acc.newsfeed.common.exception.comment;

import lombok.Getter;

@Getter
public class CommentOperationException extends RuntimeException {

    private final CommentErrorCode errorCode;
    private final String details;

    public CommentOperationException(CommentErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = null;
    }

    public CommentOperationException(CommentErrorCode errorCode, String details) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public CommentOperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = CommentErrorCode.COMMENT_CREATION_FAILED;
        this.details = null;
    }

    // Static factory methods for common scenarios
    public static CommentOperationException creationFailed() {
        return new CommentOperationException(CommentErrorCode.COMMENT_CREATION_FAILED);
    }

    public static CommentOperationException updateFailed() {
        return new CommentOperationException(CommentErrorCode.COMMENT_UPDATE_FAILED);
    }

    public static CommentOperationException deletionFailed() {
        return new CommentOperationException(CommentErrorCode.COMMENT_DELETE_FAILED);
    }

    public static CommentOperationException alreadyDeleted() {
        return new CommentOperationException(CommentErrorCode.COMMENT_ALREADY_DELETED);
    }

    public static CommentOperationException countSyncFailed() {
        return new CommentOperationException(CommentErrorCode.COMMENT_COUNT_SYNC_FAILED);
    }

    public static CommentOperationException contentEmpty() {
        return new CommentOperationException(CommentErrorCode.COMMENT_CONTENT_EMPTY);
    }

    public static CommentOperationException contentTooLong() {
        return new CommentOperationException(CommentErrorCode.COMMENT_CONTENT_TOO_LONG);
    }

    public static CommentOperationException postNotFound() {
        return new CommentOperationException(CommentErrorCode.COMMENT_POST_NOT_FOUND);
    }

    public static CommentOperationException replyDepthExceeded() {
        return new CommentOperationException(CommentErrorCode.COMMENT_REPLY_DEPTH_EXCEEDED);
    }
}