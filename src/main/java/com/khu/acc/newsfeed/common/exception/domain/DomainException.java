package com.khu.acc.newsfeed.common.exception.domain;

import com.khu.acc.newsfeed.common.exception.common.BaseException;
import com.khu.acc.newsfeed.common.exception.common.ErrorCode;

/**
 * Domain Layer 예외의 기본 클래스
 * 비즈니스 규칙 위반 시 발생
 */
public class DomainException extends BaseException {
    
    public DomainException(ErrorCode errorCode) {
        super(errorCode);
    }
    
    public DomainException(ErrorCode errorCode, String details) {
        super(errorCode, details);
    }
}