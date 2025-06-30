package com.khu.acc.newsfeed.common.exception.handler;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.exception.comment.CommentLikeAlreadyExistsException;
import com.khu.acc.newsfeed.common.exception.comment.CommentLikeOperationException;
import com.khu.acc.newsfeed.common.exception.common.ErrorResponse;
import com.khu.acc.newsfeed.common.exception.like.LikeAlreadyExistsException;
import com.khu.acc.newsfeed.common.exception.like.LikeOperationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * Like Aggregate 관련 예외 처리
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LikeExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(LikeOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleLikeOperationException(
            LikeOperationException ex, WebRequest request) {

        log.warn("Like operation failed: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getDetails());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    @ExceptionHandler(LikeAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleLikeAlreadyExistsException(
            LikeAlreadyExistsException ex, WebRequest request) {

        log.warn("Like already exists: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), 
                String.format("postId: %s, userId: %s", ex.getPostId(), ex.getUserId()));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    @ExceptionHandler(CommentLikeOperationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCommentLikeOperationException(
            CommentLikeOperationException ex, WebRequest request) {

        log.warn("Comment like operation failed: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getDetails());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    @ExceptionHandler(CommentLikeAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleCommentLikeAlreadyExistsException(
            CommentLikeAlreadyExistsException ex, WebRequest request) {

        log.warn("Comment like already exists: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), 
                String.format("commentId: %s, userId: %s", ex.getCommentId(), ex.getUserId()));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
}