package com.khu.acc.newsfeed.newsfeed.messaging;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * NewsFeed 테이블의 DynamoDB Streams 이벤트 처리
 * 뉴스피드 아이템 변경 시 필요한 처리 수행 (현재는 로깅만)
 */
@Slf4j
public class NewsFeedStreamProcessor extends DynamoDBStreamHandler {

    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        String eventName = record.getEventName();
        Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
        Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

        switch (eventName) {
            case "INSERT":
                handleNewsFeedItemCreated(newImage);
                break;
            case "MODIFY":
                handleNewsFeedItemModified(newImage, oldImage);
                break;
            case "REMOVE":
                handleNewsFeedItemDeleted(oldImage);
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    private void handleNewsFeedItemCreated(Map<String, AttributeValue> attributes) {
        String userId = getStringAttribute(attributes, "userId");
        String postId = getStringAttribute(attributes, "postId");
        String authorId = getStringAttribute(attributes, "authorId");
        Boolean isActive = getBooleanAttribute(attributes, "isActive");

        if (isActive == null || !isActive) {
            log.debug("Skipping inactive news feed item");
            return;
        }

        log.info("NewsFeed item created: userId={}, postId={}, authorId={}", 
                userId, postId, authorId);

        // 향후 확장 가능한 처리들:
        // 1. 실시간 알림 발송
        // 2. 피드 캐시 갱신
        // 3. 개인화 점수 계산
        // 4. 분석 데이터 수집
    }

    private void handleNewsFeedItemModified(Map<String, AttributeValue> newImage, Map<String, AttributeValue> oldImage) {
        String userId = getStringAttribute(newImage, "userId");
        String postId = getStringAttribute(newImage, "postId");
        
        // isActive 상태 변경 감지
        Boolean oldIsActive = getBooleanAttribute(oldImage, "isActive");
        Boolean newIsActive = getBooleanAttribute(newImage, "isActive");
        
        if ((oldIsActive != null && oldIsActive) && (newIsActive == null || !newIsActive)) {
            log.info("NewsFeed item deactivated: userId={}, postId={}", userId, postId);
            
            // 피드 아이템 비활성화 시 처리:
            // 1. 캐시에서 제거
            // 2. 실시간 피드에서 제거 알림
        } else if ((oldIsActive == null || !oldIsActive) && (newIsActive != null && newIsActive)) {
            log.info("NewsFeed item reactivated: userId={}, postId={}", userId, postId);
        }

        log.debug("NewsFeed item modified: userId={}, postId={}", userId, postId);
    }

    private void handleNewsFeedItemDeleted(Map<String, AttributeValue> attributes) {
        String userId = getStringAttribute(attributes, "userId");
        String postId = getStringAttribute(attributes, "postId");
        String authorId = getStringAttribute(attributes, "authorId");

        log.info("NewsFeed item deleted: userId={}, postId={}, authorId={}", 
                userId, postId, authorId);

        // 피드 아이템 삭제 시 처리:
        // 1. 관련 캐시 무효화
        // 2. 실시간 피드 업데이트
        // 3. 분석 데이터 정리
    }

    @Override
    protected String getPrimaryKeyName() {
        return "userId";
    }
}