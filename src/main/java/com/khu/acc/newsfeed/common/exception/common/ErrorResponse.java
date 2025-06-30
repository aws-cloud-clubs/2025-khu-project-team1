package com.khu.acc.newsfeed.common.exception.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 통일된 에러 응답 구조
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    private Instant timestamp;
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(Instant.now())
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String details) {
        return ErrorResponse.builder()
                .errorCode(errorCode.getCode())
                .message(errorCode.getMessage())
                .details(details)
                .timestamp(Instant.now())
                .build();
    }
    
    public static ErrorResponse of(BaseException exception) {
        return ErrorResponse.builder()
                .errorCode(exception.getCode())
                .message(exception.getMessage())
                .details(exception.getDetails())
                .timestamp(Instant.now())
                .build();
    }
}