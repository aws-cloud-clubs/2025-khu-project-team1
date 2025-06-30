package com.khu.acc.newsfeed.common.exception.handler;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.exception.image.ImageUploadException;
import com.khu.acc.newsfeed.common.exception.image.ImageValidationException;
import com.khu.acc.newsfeed.common.exception.image.ImageProcessingException;
import com.khu.acc.newsfeed.common.exception.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Image Aggregate 관련 예외 처리
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ImageExceptionHandler extends BaseExceptionHandler {
    
    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleImageUploadException(
            ImageUploadException ex, WebRequest request) {

        log.warn("Image upload failed: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }

    @ExceptionHandler(ImageValidationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleImageValidationException(
            ImageValidationException ex, WebRequest request) {

        log.warn("Image validation failed: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }

    @ExceptionHandler(ImageProcessingException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleImageProcessingException(
            ImageProcessingException ex, WebRequest request) {

        log.error("Image processing failed: {} [{}] at {}", ex.getMessage(), ex.getErrorCode().getCode(),
                request.getDescription(false), ex);
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Object>> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex, WebRequest request) {

        log.warn("File size exceeded: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("File size too large. Maximum allowed size is 100MB", null));
    }
}