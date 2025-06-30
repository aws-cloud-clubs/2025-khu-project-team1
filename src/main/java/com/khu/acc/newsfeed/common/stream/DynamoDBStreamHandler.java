package com.khu.acc.newsfeed.common.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.Map;

/**
 * DynamoDB Streams 이벤트 처리를 위한 공통 추상 클래스
 * 각 테이블별 구체적인 핸들러는 이 클래스를 상속받아 구현
 */
@Slf4j
public abstract class DynamoDBStreamHandler implements RequestHandler<DynamodbEvent, Void> {

    protected final SnsClient snsClient;
    protected final ObjectMapper objectMapper;
    protected final String postEventsTopicArn;

    protected DynamoDBStreamHandler() {
        this.snsClient = SnsClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.postEventsTopicArn = System.getenv("POST_EVENTS_TOPIC_ARN");
    }

    @Override
    public Void handleRequest(DynamodbEvent event, Context context) {
        log.info("Processing {} DynamoDB stream records", event.getRecords().size());

        for (DynamodbEvent.DynamodbStreamRecord record : event.getRecords()) {
            try {
                processRecord(record, context);
            } catch (Exception e) {
                log.error("Failed to process DynamoDB stream record: {}", record.getEventID(), e);
                // 개별 레코드 실패가 전체 배치를 실패시키지 않도록 처리
                // 추후 Dead Letter Queue나 재시도 로직 추가 가능
            }
        }

        return null;
    }

    /**
     * 개별 DynamoDB Stream 레코드 처리
     * 하위 클래스에서 구체적인 구현 제공
     */
    protected abstract void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context);

    /**
     * 도메인 이벤트를 SNS로 발행
     */
    protected void publishToSns(Object domainEvent) {
        try {
            String messageBody = objectMapper.writeValueAsString(domainEvent);
            
            PublishRequest publishRequest = PublishRequest.builder()
                    .topicArn(postEventsTopicArn)
                    .message(messageBody)
                    .build();

            PublishResponse response = snsClient.publish(publishRequest);
            log.info("Published {} event to SNS, MessageId: {}", domainEvent.getClass().getSimpleName(), response.messageId());

        } catch (Exception e) {
            log.error("Failed to publish {} event to SNS", domainEvent.getClass().getSimpleName(), e);
            throw new RuntimeException("SNS publishing failed", e);
        }
    }

    /**
     * DynamoDB 속성값을 Java 객체로 변환하는 유틸리티 메서드
     */
    protected String getStringAttribute(Map<String, AttributeValue> attributes, String key) {
        AttributeValue value = attributes.get(key);
        return value != null ? value.getS() : null;
    }

    protected Long getLongAttribute(Map<String, AttributeValue> attributes, String key) {
        AttributeValue value = attributes.get(key);
        return value != null ? Long.parseLong(value.getN()) : null;
    }

    protected Boolean getBooleanAttribute(Map<String, AttributeValue> attributes, String key) {
        AttributeValue value = attributes.get(key);
        return value != null ? value.getBOOL() : null;
    }

    protected java.time.Instant getInstantAttribute(Map<String, AttributeValue> attributes, String key) {
        String timestamp = getStringAttribute(attributes, key);
        return timestamp != null ? java.time.Instant.parse(timestamp) : java.time.Instant.now();
    }

    protected java.util.List<String> getStringListAttribute(Map<String, AttributeValue> attributes, String key) {
        AttributeValue value = attributes.get(key);
        if (value != null && value.getSS() != null) {
            return value.getSS();
        }
        return java.util.List.of();
    }

    /**
     * 이벤트 아이템인지 확인 (PK가 EVENT#로 시작하는지)
     */
    protected boolean isEventItem(Map<String, AttributeValue> keys) {
        String primaryKey = getStringAttribute(keys, getPrimaryKeyName());
        return primaryKey != null && primaryKey.startsWith("EVENT#");
    }

    /**
     * 실제 엔티티 아이템인지 확인 (이벤트 아이템이 아닌)
     */
    protected boolean isEntityItem(Map<String, AttributeValue> keys) {
        return !isEventItem(keys);
    }

    /**
     * 각 테이블의 Primary Key 이름 반환
     * 하위 클래스에서 구현
     */
    protected abstract String getPrimaryKeyName();

    /**
     * 로그용 컨텍스트 정보 생성
     */
    protected void logRecordInfo(DynamodbEvent.DynamodbStreamRecord record) {
        String eventName = record.getEventName();
        String primaryKey = getStringAttribute(record.getDynamodb().getKeys(), getPrimaryKeyName());
        
        log.info("Processing {} event for {}: {}", 
                eventName, getPrimaryKeyName(), primaryKey);
    }
}