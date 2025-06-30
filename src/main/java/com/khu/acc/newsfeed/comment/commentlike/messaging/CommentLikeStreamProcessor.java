package com.khu.acc.newsfeed.comment.commentlike.messaging;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.comment.commentlike.messaging.event.CommentLikeCreatedEvent;
import com.khu.acc.newsfeed.comment.commentlike.messaging.event.CommentLikeDeletedEvent;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

/**
 * CommentLikes 테이블의 DynamoDB Streams 이벤트 처리
 * 댓글 좋아요 생성/삭제 시 도메인 이벤트를 SNS로 발행
 */
@Slf4j
public class CommentLikeStreamProcessor extends DynamoDBStreamHandler {

    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        if (isEventItem(record.getDynamodb().getKeys())) {
            log.debug("Skipping event item");
            return;
        }

        String eventName = record.getEventName();
        Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
        Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

        switch (eventName) {
            case "INSERT":
                handleCommentLikeCreated(newImage);
                break;
            case "REMOVE":
                handleCommentLikeDeleted(oldImage);
                break;
            case "MODIFY":
                log.debug("Ignoring MODIFY event for CommentLike entity");
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    private void handleCommentLikeCreated(Map<String, AttributeValue> attributes) {
        // isActive가 true인 경우만 처리
        Boolean isActive = getBooleanAttribute(attributes, "isActive");
        if (isActive == null || !isActive) {
            log.debug("Skipping inactive comment like");
            return;
        }

        CommentLikeCreatedEvent event = new CommentLikeCreatedEvent(
                getStringAttribute(attributes, "commentId"),
                getStringAttribute(attributes, "userId"),
                getStringAttribute(attributes, "commentLikeId"),
                getInstantAttribute(attributes, "createdAt")
        );

        publishToSns(event);
        log.info("Published CommentLikeCreatedEvent for commentId: {}, userId: {}", 
                event.getCommentId(), event.getUserId());
    }

    private void handleCommentLikeDeleted(Map<String, AttributeValue> attributes) {
        CommentLikeDeletedEvent event = new CommentLikeDeletedEvent(
                getStringAttribute(attributes, "commentId"),
                getStringAttribute(attributes, "userId")
        );

        publishToSns(event);
        log.info("Published CommentLikeDeletedEvent for commentId: {}, userId: {}", 
                event.getCommentId(), event.getUserId());
    }


    @Override
    protected String getPrimaryKeyName() {
        return "commentLikeId";
    }
}