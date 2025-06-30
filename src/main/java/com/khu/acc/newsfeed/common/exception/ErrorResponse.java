package com.khu.acc.newsfeed.common.exception;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 통합 에러 응답 클래스
 * 모든 ErrorCode 구현체에 대해 사용 가능
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    private Instant timestamp;
    
    // Static factory methods
    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, null);
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String details) {
        ErrorResponse response = new ErrorResponse();
        response.errorCode = errorCode.getCode();
        response.message = errorCode.getMessage();
        response.details = details;
        response.timestamp = Instant.now();
        return response;
    }
    
    public static ErrorResponse ofWithFormattedMessage(ErrorCode errorCode, String details, Object... args) {
        ErrorResponse response = new ErrorResponse();
        response.errorCode = errorCode.getCode();
        response.message = String.format(errorCode.getMessage(), args);
        response.details = details;
        response.timestamp = Instant.now();
        return response;
    }
}