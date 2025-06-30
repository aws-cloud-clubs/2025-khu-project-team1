package com.khu.acc.newsfeed.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khu.acc.newsfeed.post.postlike.messaging.event.LikeCreatedEvent;
import com.khu.acc.newsfeed.post.postlike.messaging.event.LikeDeletedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * SQS에서 좋아요 이벤트를 받아서 DynamoDB 버퍼 테이블에 델타 값을 누적하는 Lambda 함수
 * 실제 Post.likesCount 업데이트는 별도의 배치 처리 Lambda에서 수행
 */
@Slf4j
public class LikeEventProcessor implements RequestHandler<SQSEvent, Void> {

    private final DynamoDBMapper dynamoDBMapper;
    private final ObjectMapper objectMapper;

    public LikeEventProcessor() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
        this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient, DynamoDBMapperConfig.DEFAULT);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        log.info("Processing {} SQS records for like count buffering", event.getRecords().size());

        for (SQSEvent.SQSMessage record : event.getRecords()) {
            try {
                processSQSRecord(record);
            } catch (Exception e) {
                log.error("Failed to process SQS record: {}", record.getMessageId(), e);
                // 개별 레코드 실패가 전체 배치를 실패시키지 않도록 처리
            }
        }

        return null;
    }

    private void processSQSRecord(SQSEvent.SQSMessage record) throws Exception {
        String messageBody = record.getBody();
        String eventType = getEventTypeFromMessage(messageBody);

        log.debug("Processing like event: {}", eventType);

        switch (eventType) {
            case "LIKE_CREATED":
                handleLikeCreated(objectMapper.readValue(messageBody, LikeCreatedEvent.class));
                break;
            case "LIKE_DELETED":
                handleLikeDeleted(objectMapper.readValue(messageBody, LikeDeletedEvent.class));
                break;
            default:
                log.warn("Unknown event type in like event processor: {}", eventType);
        }
    }

    private String getEventTypeFromMessage(String messageBody) {
        try {
            // SNS 메시지 형태로 오는 경우 파싱
            if (messageBody.contains("\"Type\"") && messageBody.contains("\"Message\"")) {
                var node = objectMapper.readTree(messageBody);
                String actualMessage = node.get("Message").asText();
                var eventNode = objectMapper.readTree(actualMessage);
                return eventNode.get("eventType").asText();
            } else {
                // 직접 이벤트 메시지인 경우
                var eventNode = objectMapper.readTree(messageBody);
                return eventNode.get("eventType").asText();
            }
        } catch (Exception e) {
            log.warn("Failed to extract event type from message: {}", messageBody, e);
            return "UNKNOWN";
        }
    }

    private void handleLikeCreated(LikeCreatedEvent event) {
        String postId = event.getPostId();
        log.debug("Handling LikeCreatedEvent for post: {}", postId);

        try {
            updateLikeCountBuffer(postId, 1);
            log.debug("Successfully buffered like increment for post: {}", postId);
        } catch (Exception e) {
            log.error("Failed to buffer like increment for post: {}", postId, e);
            throw e;
        }
    }

    private void handleLikeDeleted(LikeDeletedEvent event) {
        String postId = event.getPostId();
        log.debug("Handling LikeDeletedEvent for post: {}", postId);

        try {
            updateLikeCountBuffer(postId, -1);
            log.debug("Successfully buffered like decrement for post: {}", postId);
        } catch (Exception e) {
            log.error("Failed to buffer like decrement for post: {}", postId, e);
            throw e;
        }
    }

    private void updateLikeCountBuffer(String postId, int delta) {
        try {
            // 기존 버퍼 레코드 조회 또는 새로 생성
            LikeCountBuffer buffer = dynamoDBMapper.load(LikeCountBuffer.class, postId);
            
            if (buffer == null) {
                // 새로운 버퍼 레코드 생성
                buffer = new LikeCountBuffer();
                buffer.setPostId(postId);
                buffer.setDeltaCount((long) delta);
                buffer.setLastUpdated(Instant.now().toString());
                buffer.setVersion(1L);
            } else {
                // 기존 버퍼에 델타 누적
                if (delta > 0) {
                    buffer.incrementDelta(delta);
                } else {
                    buffer.decrementDelta(-delta);
                }
            }

            // DynamoDB에 저장 (Atomic 업데이트)
            dynamoDBMapper.save(buffer);
            
            log.debug("Updated like count buffer for post {}: deltaCount={}, version={}", 
                    postId, buffer.getDeltaCount(), buffer.getVersion());

        } catch (Exception e) {
            log.error("Failed to update like count buffer for post: {}", postId, e);
            throw e;
        }
    }
}