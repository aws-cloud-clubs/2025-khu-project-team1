package com.khu.acc.newsfeed.common.exception.application.post;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;
import com.khu.acc.newsfeed.common.exception.post.PostErrorCode;
import lombok.Getter;

/**
 * 포스트 접근 권한이 없을 때 발생하는 예외
 */
@Getter
public class PostAccessDeniedException extends ApplicationException {
    
    private final String postId;
    private final String userId;
    
    public PostAccessDeniedException(PostErrorCode errorCode) {
        super(errorCode);
        this.postId = null;
        this.userId = null;
    }
    
    public PostAccessDeniedException(PostErrorCode errorCode, String postId, String userId) {
        super(errorCode, String.format("Post: %s, User: %s", postId, userId));
        this.postId = postId;
        this.userId = userId;
    }
    
}