package com.khu.acc.newsfeed.common.exception.comment;

import lombok.Getter;

@Getter
public class CommentLikeAlreadyExistsException extends RuntimeException {

    private final CommentErrorCode errorCode;
    private final String commentId;
    private final String userId;

    public CommentLikeAlreadyExistsException(String commentId, String userId) {
        super(CommentErrorCode.COMMENT_LIKE_ALREADY_EXISTS.getCodeWithMessage());
        this.errorCode = CommentErrorCode.COMMENT_LIKE_ALREADY_EXISTS;
        this.commentId = commentId;
        this.userId = userId;
    }

    public CommentLikeAlreadyExistsException(CommentErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.commentId = null;
        this.userId = null;
    }

    // Static factory methods
    public static CommentLikeAlreadyExistsException of(String commentId, String userId) {
        return new CommentLikeAlreadyExistsException(commentId, userId);
    }
}