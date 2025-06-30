package com.khu.acc.newsfeed.follow;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Follows 테이블의 DynamoDB Streams 이벤트 처리
 * 팔로우/언팔로우 시 로깅 및 향후 확장 포인트 제공
 */
@Slf4j
public class FollowStreamProcessor extends DynamoDBStreamHandler {

    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        // 실제 Follow 엔티티 변경만 처리 (EVENT# 아이템은 제외)
        if (isEventItem(record.getDynamodb().getKeys())) {
            log.debug("Skipping event item");
            return;
        }

        String eventName = record.getEventName();
        Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
        Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

        switch (eventName) {
            case "INSERT":
                handleFollowCreated(newImage);
                break;
            case "REMOVE":
                handleFollowDeleted(oldImage);
                break;
            case "MODIFY":
                handleFollowModified(newImage, oldImage);
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    private void handleFollowCreated(Map<String, AttributeValue> attributes) {
        // isActive가 true인 경우만 처리
        Boolean isActive = getBooleanAttribute(attributes, "isActive");
        if (isActive == null || !isActive) {
            log.debug("Skipping inactive follow");
            return;
        }

        String followId = getStringAttribute(attributes, "followId");
        String followerId = getStringAttribute(attributes, "followerId");
        String followeeId = getStringAttribute(attributes, "followeeId");

        log.info("Follow created: followId={}, followerId={}, followeeId={}", 
                followId, followerId, followeeId);

        // 향후 확장 가능한 처리들:
        // 1. 팔로우 알림 발송
        // 2. 팔로우 수 업데이트
        // 3. 추천 시스템 업데이트
    }

    private void handleFollowDeleted(Map<String, AttributeValue> attributes) {
        String followId = getStringAttribute(attributes, "followId");
        String followerId = getStringAttribute(attributes, "followerId");
        String followeeId = getStringAttribute(attributes, "followeeId");

        log.info("Follow deleted: followerId={}, followeeId={}", followerId, followeeId);

        // 향후 확장 가능한 처리들:
        // 1. 언팔로우 알림 발송 (선택적)
        // 2. 팔로우 수 업데이트
        // 3. 추천 시스템 업데이트
    }

    private void handleFollowModified(Map<String, AttributeValue> newImage, Map<String, AttributeValue> oldImage) {
        String followId = getStringAttribute(newImage, "followId");
        String followerId = getStringAttribute(newImage, "followerId");
        String followeeId = getStringAttribute(newImage, "followeeId");
        
        // isActive 상태 변경 감지
        Boolean oldIsActive = getBooleanAttribute(oldImage, "isActive");
        Boolean newIsActive = getBooleanAttribute(newImage, "isActive");
        
        if (oldIsActive != null && oldIsActive && (newIsActive == null || !newIsActive)) {
            // 팔로우 비활성화 (소프트 언팔로우)
            log.info("Follow deactivated: followerId={}, followeeId={}", followerId, followeeId);
        } else if ((oldIsActive == null || !oldIsActive) && newIsActive != null && newIsActive) {
            // 팔로우 재활성화
            log.info("Follow reactivated: followerId={}, followeeId={}", followerId, followeeId);
        }

        log.debug("Follow modified: followId={}", followId);
    }


    @Override
    protected String getPrimaryKeyName() {
        return "followId";
    }
}