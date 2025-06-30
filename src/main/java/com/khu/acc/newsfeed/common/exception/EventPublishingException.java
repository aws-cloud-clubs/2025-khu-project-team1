package com.khu.acc.newsfeed.common.exception;

/**
 * 이벤트 발행 실패 시 발생하는 예외
 */
public class EventPublishingException extends RuntimeException {
    
    public EventPublishingException(String message) {
        super(message);
    }
    
    public EventPublishingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static EventPublishingException sqsMessageFailed(String queueName, Throwable cause) {
        return new EventPublishingException(
                String.format("Failed to send message to SQS queue: %s", queueName), 
                cause
        );
    }
    
    public static EventPublishingException eventProcessingFailed(String eventType, Throwable cause) {
        return new EventPublishingException(
                String.format("Failed to process event: %s", eventType), 
                cause
        );
    }
}