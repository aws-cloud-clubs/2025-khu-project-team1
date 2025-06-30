package com.khu.acc.newsfeed.common.exception.application.post;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;
import com.khu.acc.newsfeed.common.exception.post.PostErrorCode;
import lombok.Getter;

/**
 * 포스트를 찾을 수 없을 때 발생하는 예외
 */
@Getter
public class PostNotFoundException extends ApplicationException {
    
    private final String postId;
    
    public PostNotFoundException(String postId) {
        super(PostErrorCode.POST_NOT_FOUND, postId);
        this.postId = postId;
    }
    
    public PostNotFoundException(PostErrorCode errorCode, String postId) {
        super(errorCode, postId);
        this.postId = postId;
    }
    
}