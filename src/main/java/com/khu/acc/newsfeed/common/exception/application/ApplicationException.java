package com.khu.acc.newsfeed.common.exception.application;

import com.khu.acc.newsfeed.common.exception.common.BaseException;
import com.khu.acc.newsfeed.common.exception.common.ErrorCode;

/**
 * Application Layer 예외의 기본 클래스
 * 유스케이스 실행 중 발생하는 예외
 */
public class ApplicationException extends BaseException {
    
    public ApplicationException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public ApplicationException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
    
    public ApplicationException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}