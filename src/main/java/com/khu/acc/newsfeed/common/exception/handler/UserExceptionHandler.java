package com.khu.acc.newsfeed.common.exception.handler;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.exception.user.UserNotFoundException;
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
 * User Aggregate 관련 예외 처리
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserNotFoundException(
            UserNotFoundException ex, WebRequest request) {

        log.warn("User not found: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(), 
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getUserId());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
}