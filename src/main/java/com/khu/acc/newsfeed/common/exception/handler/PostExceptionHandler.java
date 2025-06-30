package com.khu.acc.newsfeed.common.exception.handler;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.exception.application.post.PostAccessDeniedException;
import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.common.exception.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Post Aggregate 관련 예외 처리
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PostExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handlePostNotFoundException(
            PostNotFoundException ex, WebRequest request) {
        
        log.warn("Post not found: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(), 
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getPostId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    @ExceptionHandler(PostAccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handlePostAccessDeniedException(
            PostAccessDeniedException ex, WebRequest request) {
        
        log.warn("Post access denied: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(), 
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getPostId());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
}