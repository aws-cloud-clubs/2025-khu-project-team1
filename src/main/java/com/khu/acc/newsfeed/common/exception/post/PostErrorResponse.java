package com.khu.acc.newsfeed.common.exception.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostErrorResponse {
    
    private String errorCode;
    private String message;
    private String details;
    private Instant timestamp;
    
    // Static factory methods
    public static PostErrorResponse of(PostErrorCode errorCode) {
        return of(errorCode, null);
    }
    
    public static PostErrorResponse of(PostErrorCode errorCode, String details) {
        PostErrorResponse response = new PostErrorResponse();
        response.errorCode = errorCode.getCode();
        response.message = errorCode.getMessage();
        response.details = details;
        response.timestamp = Instant.now();
        return response;
    }
    
    public static PostErrorResponse ofWithFormattedMessage(PostErrorCode errorCode, String details, Object... args) {
        PostErrorResponse response = new PostErrorResponse();
        response.errorCode = errorCode.getCode();
        response.message = String.format(errorCode.getMessage(), args);
        response.details = details;
        response.timestamp = Instant.now();
        return response;
    }
}