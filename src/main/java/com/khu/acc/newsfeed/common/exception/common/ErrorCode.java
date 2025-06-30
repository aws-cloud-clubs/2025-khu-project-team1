package com.khu.acc.newsfeed.common.exception.common;

/**
 * 에러 코드 인터페이스
 * 각 aggregate별로 구현하여 사용
 */
public interface ErrorCode {
    String getCode();
    String getMessage();
    
    /**
     * 에러 코드와 메시지를 조합한 문자열 반환
     * 형식: "[코드] 메시지"
     */
    default String getCodeWithMessage() {
        return String.format("[%s] %s", getCode(), getMessage());
    }
}