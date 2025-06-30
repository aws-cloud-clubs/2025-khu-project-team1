package com.khu.acc.newsfeed.common.exception;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;
import com.khu.acc.newsfeed.common.exception.common.BaseException;
import com.khu.acc.newsfeed.common.exception.common.ErrorResponse;
import com.khu.acc.newsfeed.common.exception.domain.DomainException;
import com.khu.acc.newsfeed.common.exception.infrastructure.InfrastructureException;
import com.khu.acc.newsfeed.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Layer별 기본 처리
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDomainException(
            DomainException ex, WebRequest request) {
        return handleBaseException(ex, HttpStatus.BAD_REQUEST, request);
    }
    
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleApplicationException(
            ApplicationException ex, WebRequest request) {
        
        HttpStatus status = determineStatusFromException(ex);
        return handleBaseException(ex, status, request);
    }
    
    @ExceptionHandler(InfrastructureException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleInfrastructureException(
            InfrastructureException ex, WebRequest request) {
        
        log.error("Infrastructure exception: {} at {}", ex.getMessage(), 
                request.getDescription(false), ex);
        return handleBaseException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.warn("Illegal argument: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Object>> handleIllegalStateException(
            IllegalStateException ex, WebRequest request) {

        log.warn("Illegal state: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {

        log.warn("Authentication failed: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication failed: " + ex.getMessage(), null));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentialsException(
            BadCredentialsException ex, WebRequest request) {

        log.warn("Bad credentials: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid credentials", null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {

        log.warn("Access denied: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Access denied: " + ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        log.warn("Resource not found: {} at {}", ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {

        log.error("Runtime exception: {} at {}", ex.getMessage(), request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {

        log.error("Unexpected exception: {} at {}", ex.getMessage(), request.getDescription(false), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error", null));
    }

    // Helper methods
    private ResponseEntity<ApiResponse<ErrorResponse>> handleBaseException(
            BaseException ex, HttpStatus status, WebRequest request) {
        
        log.warn("Base exception: {} [{}] at {}", ex.getMessage(), ex.getCode(), 
                request.getDescription(false));
        
        ErrorResponse errorResponse = ErrorResponse.of(ex.getErrorCode(), ex.getDetails());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage(), errorResponse));
    }
    
    private HttpStatus determineStatusFromException(BaseException ex) {
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
