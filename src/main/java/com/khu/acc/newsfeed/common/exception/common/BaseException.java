package com.khu.acc.newsfeed.common.exception.common;

import lombok.Getter;

/**
 * 모든 커스텀 예외의 기본 클래스
 * 에러 코드와 상세 정보를 담는 공통 구조 제공
 */
@Getter
public abstract class BaseException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String details;
    
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = null;
    }
    
    protected BaseException(ErrorCode errorCode, String details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
    
    protected BaseException(ErrorCode errorCode, String details, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public String getCode() {
        return errorCode.getCode();
    }
    
    public String getFormattedMessage() {
        return String.format("[%s] %s", errorCode.getCode(), getMessage());
    }
}