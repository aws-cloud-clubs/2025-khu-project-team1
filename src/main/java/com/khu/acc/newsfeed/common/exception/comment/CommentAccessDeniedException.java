package com.khu.acc.newsfeed.common.exception.comment;

import lombok.Getter;

@Getter
public class CommentAccessDeniedException extends RuntimeException {

    private final CommentErrorCode errorCode;
    private final String commentId;
    private final String userId;

    public CommentAccessDeniedException(CommentErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.commentId = null;
        this.userId = null;
    }

    public CommentAccessDeniedException(CommentErrorCode errorCode, String commentId, String userId) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.commentId = commentId;
        this.userId = userId;
    }

    // Static factory methods
    public static CommentAccessDeniedException accessDenied() {
        return new CommentAccessDeniedException(CommentErrorCode.COMMENT_ACCESS_DENIED);
    }

    public static CommentAccessDeniedException updateDenied() {
        return new CommentAccessDeniedException(CommentErrorCode.COMMENT_UPDATE_ACCESS_DENIED);
    }

    public static CommentAccessDeniedException deleteDenied() {
        return new CommentAccessDeniedException(CommentErrorCode.COMMENT_DELETE_ACCESS_DENIED);
    }

    public static CommentAccessDeniedException ownerMismatch(String commentId, String userId) {
        return new CommentAccessDeniedException(CommentErrorCode.COMMENT_OWNER_MISMATCH, commentId, userId);
    }
}