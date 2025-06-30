package com.khu.acc.newsfeed.common.exception.comment;

import lombok.Getter;

@Getter
public class CommentNotFoundException extends RuntimeException {

    private final CommentErrorCode errorCode;
    private final String commentId;

    public CommentNotFoundException(String commentId) {
        super(String.format("%s - Comment ID: %s", CommentErrorCode.COMMENT_NOT_FOUND.getCodeWithMessage(), commentId));
        this.errorCode = CommentErrorCode.COMMENT_NOT_FOUND;
        this.commentId = commentId;
    }

    public CommentNotFoundException(CommentErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.commentId = null;
    }

    public CommentNotFoundException(CommentErrorCode errorCode, String commentId) {
        super(String.format("%s - Comment ID: %s", errorCode.getCodeWithMessage(), commentId));
        this.errorCode = errorCode;
        this.commentId = commentId;
    }

    // Static factory methods
    public static CommentNotFoundException notFound(String commentId) {
        return new CommentNotFoundException(commentId);
    }

    public static CommentNotFoundException deleted(String commentId) {
        return new CommentNotFoundException(CommentErrorCode.COMMENT_DELETED, commentId);
    }

    public static CommentNotFoundException inactive(String commentId) {
        return new CommentNotFoundException(CommentErrorCode.COMMENT_INACTIVE, commentId);
    }

    public static CommentNotFoundException parentNotFound(String parentCommentId) {
        return new CommentNotFoundException(CommentErrorCode.PARENT_COMMENT_NOT_FOUND, parentCommentId);
    }
}