package com.khu.acc.newsfeed.post.postlike.messaging;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.postlike.domain.Like;
import com.khu.acc.newsfeed.post.postlike.messaging.event.LikeCreatedEvent;
import com.khu.acc.newsfeed.post.postlike.messaging.event.LikeDeletedEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Likes 테이블의 DynamoDB Streams 이벤트 처리
 * 완전한 AWS SDK v2 네이티브 환경 - Enhanced Client 직접 활용
 * SNS로 최소 정보 이벤트 발행, Message Filtering을 통해 알림/카운터 서비스에 선별적 전달
 */
@Slf4j
public class LikeStreamProcessor extends DynamoDBStreamHandler {

    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        // 실제 Like 엔티티 변경만 처리 (EVENT# 아이템은 제외)
        if (isEventItem(record.getDynamodb().getKeys())) {
            log.debug("Skipping event item");
            return;
        }

        String eventName = record.getEventName();

        switch (eventName) {
            case "INSERT":
                handleLikeCreated(Objects.requireNonNull(unmarshallToLike(record.getDynamodb().getNewImage())));
                break;
            case "REMOVE":
                handleLikeDeleted(unmarshallToLike(record.getDynamodb().getOldImage()));
                break;
            case "MODIFY":
                log.debug("Ignoring MODIFY event for Like entity");
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    private Like unmarshallToLike(Map<String, AttributeValue> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            log.debug("AttributeMap is null or empty");
            return null;
        }

        try {
            Like like = new Like();

            // Required fields
            like.setPostId(getStringValue(attributeMap, "postId"));
            like.setLikeId(getStringValue(attributeMap, "postId"));
            like.setUserId(getStringValue(attributeMap, "userId"));
            like.setCreatedAt(getInstantValue(attributeMap, "createdAt"));

            log.debug("Successfully unmarshalled Like: {}", like.getLikeId());
            return like;

        } catch (Exception e) {
            log.error("Failed to unmarshall Like object from DynamoDB Stream record: {}", attributeMap, e);
            return null;
        }
    }

    /**
     * AttributeValue에서 String 값 추출
     */
    private String getStringValue(Map<String, AttributeValue> attributeMap, String key) {
        AttributeValue value = attributeMap.get(key);
        return (value != null && value.getS() != null) ? value.getS() : null;
    }

    private Instant getInstantValue(Map<String, AttributeValue> attributeMap, String key) {
        String stringValue = getStringValue(attributeMap, key);
        if (stringValue != null && !stringValue.isEmpty()) {
            try {
                return Instant.parse(stringValue);
            } catch (Exception e) {
                log.warn("Failed to parse Instant value for key {}: {}", key, stringValue);
            }
        }
        return null;
    }

    /**
     * ★ 이제 메서드 파라미터가 타입-세이프한 객체로 변경됨
     * 더 이상 getStringAttribute 같은 헬퍼 메서드가 필요 없음!
     */
    private void handleLikeCreated(Like like) {

        // 타입-세이프한 객체 접근
        LikeCreatedEvent event = new LikeCreatedEvent(
                like.getPostId(),
                like.getUserId(),
                like.getLikeId(),
                like.getCreatedAt()
        );

        publishToSns(event);
        log.info("Published LikeCreatedEvent for postId: {}, userId: {}", like.getPostId(), like.getUserId());
    }

    private void handleLikeDeleted(Like like) {
        if (like == null) return;

        LikeDeletedEvent event = new LikeDeletedEvent(
                like.getPostId(),
                like.getUserId()
        );
        
        publishToSns(event);
        log.info("Published LikeDeletedEvent for postId: {}, userId: {}", like.getPostId(), like.getUserId());
    }

    @Override
    protected String getPrimaryKeyName() {
        return "likeId";
    }
}