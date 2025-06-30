package com.khu.acc.newsfeed.common.exception.infrastructure;

import com.khu.acc.newsfeed.common.exception.common.BaseException;
import com.khu.acc.newsfeed.common.exception.common.ErrorCode;

/**
 * Infrastructure Layer 예외의 기본 클래스
 * 외부 시스템 연동 중 발생하는 예외
 */
public class InfrastructureException extends BaseException {
    
    public InfrastructureException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public InfrastructureException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
    
    public InfrastructureException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode, details, cause);
    }
}