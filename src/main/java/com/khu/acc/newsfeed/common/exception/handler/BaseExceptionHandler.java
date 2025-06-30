package com.khu.acc.newsfeed.common.exception.handler;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.exception.common.BaseException;
import com.khu.acc.newsfeed.common.exception.common.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 공통 예외 처리를 위한 기본 핸들러
 */
@Slf4j
public abstract class BaseExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }
    
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Binding error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid request parameters", errors));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException ex) {
        
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        
        log.warn("Constraint violation: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Constraint violation", errors));
    }
    
    /**
     * BaseException을 처리하는 공통 메서드
     */
    protected ResponseEntity<ApiResponse<ErrorResponse>> handleBaseException(
            BaseException ex, HttpStatus status, WebRequest request) {
        
        log.warn("{} at {}: {} [{}]", 
                ex.getClass().getSimpleName(), 
                request.getDescription(false), 
                ex.getMessage(), 
                ex.getCode());
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getDetails());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }

    /**
     * 예외 코드 기반으로 HTTP 상태 코드를 결정
     */
    protected HttpStatus determineStatusFromException(BaseException ex) {
        String code = ex.getCode();
        
        // 404 - Not Found
        if (code.contains("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        }
        
        // 403 - Forbidden
        if (code.contains("ACCESS_DENIED") || code.contains("DENIED")) {
            return HttpStatus.FORBIDDEN;
        }
        
        // 409 - Conflict
        if (code.contains("ALREADY_EXISTS") || code.contains("EXISTS")) {
            return HttpStatus.CONFLICT;
        }
        
        // 400 - Bad Request (기본)
        return HttpStatus.BAD_REQUEST;
    }
}