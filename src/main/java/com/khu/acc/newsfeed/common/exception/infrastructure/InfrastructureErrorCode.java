package com.khu.acc.newsfeed.common.exception.infrastructure;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Infrastructure 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum InfrastructureErrorCode implements ErrorCode {
    
    // Event Publishing Errors (I001-I099)
    EVENT_PUBLISH_FAILED("I001", "Failed to publish event"),
    EVENT_SERIALIZATION_FAILED("I002", "Failed to serialize event"),
    EVENT_QUEUE_UNAVAILABLE("I003", "Event queue is unavailable"),
    
    // External Service Errors (I100-I199)
    EXTERNAL_SERVICE_UNAVAILABLE("I100", "External service is unavailable"),
    EXTERNAL_SERVICE_TIMEOUT("I101", "External service request timed out"),
    EXTERNAL_SERVICE_ERROR("I102", "External service returned an error"),
    
    // Database Errors (I200-I299)
    DATABASE_CONNECTION_FAILED("I200", "Failed to connect to database"),
    DATABASE_QUERY_FAILED("I201", "Database query failed"),
    DATABASE_TRANSACTION_FAILED("I202", "Database transaction failed"),
    
    // Cache Errors (I300-I399)
    CACHE_CONNECTION_FAILED("I300", "Failed to connect to cache"),
    CACHE_OPERATION_FAILED("I301", "Cache operation failed");
    
    private final String code;
    private final String message;
}