package com.khu.acc.newsfeed.comment.comment.messaging;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.comment.comment.messaging.event.CommentCreatedEvent;
import com.khu.acc.newsfeed.comment.comment.messaging.event.CommentDeletedEvent;
import com.khu.acc.newsfeed.comment.comment.messaging.event.CommentUpdatedEvent;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

/**
 * Comments 테이블의 DynamoDB Streams 이벤트를 처리합니다.
 * 댓글 생성/수정/삭제 시 관련 도메인 이벤트를 SNS로 발행하여
 * 포스트의 댓글 수 업데이트 등 후속 조치를 트리거합니다.
 */
@Slf4j
public class CommentStreamProcessor extends DynamoDBStreamHandler {

    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        // 실제 Comment 엔티티만 처리 (EVENT# 아이템은 무시)
        if (isEventItem(record.getDynamodb().getKeys())) {
            log.debug("Skipping EVENT# item");
            return;
        }

        String eventName = record.getEventName();
        Map<String, AttributeValue> newImage = record.getDynamodb().getNewImage();
        Map<String, AttributeValue> oldImage = record.getDynamodb().getOldImage();

        switch (eventName) {
            case "INSERT":
                handleCommentCreated(unmarshallComment(newImage));
                break;
            case "MODIFY":
                handleCommentModified(unmarshallComment(newImage), unmarshallComment(oldImage));
                break;
            case "REMOVE":
                handleCommentDeleted(unmarshallComment(oldImage));
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    /**
     * 댓글 생성 이벤트를 처리합니다.
     * 활성화된 댓글에 대해 CommentCreatedEvent를 발행하여 포스트의 댓글 수를 증가시킵니다.
     *
     * @param createdComment 새로 생성된 Comment 객체
     */
    private void handleCommentCreated(Comment createdComment) {
        if (createdComment == null || !createdComment.isActive()) {
            log.debug("Skipping event for null or inactive comment creation.");
            return;
        }

        CommentCreatedEvent event = new CommentCreatedEvent(
                createdComment.getCommentId(),
                createdComment.getPostId(),
                createdComment.getUserId(),
                createdComment.getCreatedAt()
        );

        publishToSns(event);
        log.info("Published CommentCreatedEvent for commentId: {}, postId: {}",
                createdComment.getCommentId(), createdComment.getPostId());
    }

    /**
     * 댓글 수정 이벤트를 처리합니다.
     * 댓글이 비활성화(소프트 삭제)되는 경우 CommentDeletedEvent를 발행합니다.
     *
     * @param newComment 수정 후 Comment 객체
     * @param oldComment 수정 전 Comment 객체
     */
    private void handleCommentModified(Comment newComment, Comment oldComment) {
        if (newComment == null || oldComment == null) {
            log.warn("Skipping modify event due to null newImage or oldImage.");
            return;
        }

        // 댓글이 비활성화(소프트 삭제)되는 상태 변화를 감지
        boolean wasActive = oldComment.isActive();
        boolean isNowInactive = !newComment.isActive();

        if (wasActive && isNowInactive) {
            log.info("Comment soft-deleted: commentId={}", newComment.getCommentId());
            CommentDeletedEvent event = new CommentDeletedEvent(
                    newComment.getCommentId(),
                    newComment.getPostId(),
                    newComment.getUserId(),
                    newComment.getUpdatedAt() // 삭제 시점은 updatedAt으로 간주
            );
            publishToSns(event);
        } else if (newComment.isActive()) {
            // 내용 수정 등 기타 업데이트 이벤트 처리 (필요시)
            CommentUpdatedEvent event = new CommentUpdatedEvent(
                    newComment.getCommentId(),
                    newComment.getPostId(),
                    newComment.getUserId(),
                    newComment.getUpdatedAt()
            );
            publishToSns(event);
            log.info("Published CommentUpdatedEvent for commentId: {}", newComment.getCommentId());
        }
    }

    /**
     * 댓글 물리 삭제(Hard Delete) 이벤트를 처리합니다.
     *
     * @param deletedComment 삭제된 Comment 객체
     */
    private void handleCommentDeleted(Comment deletedComment) {
        if (deletedComment == null) {
            log.warn("Skipping delete event due to null oldImage.");
            return;
        }

        log.info("Comment hard-deleted: commentId={}, postId={}",
                deletedComment.getCommentId(), deletedComment.getPostId());

        CommentDeletedEvent event = new CommentDeletedEvent(
                deletedComment.getCommentId(),
                deletedComment.getPostId(),
                deletedComment.getUserId(),
                Instant.now()
        );
        publishToSns(event);
    }

    /**
     * DynamoDB Stream의 AttributeValue Map을 타입-세이프한 Comment 객체로 변환합니다.
     *
     * @param attributeMap DynamoDB 아이템의 속성 맵
     * @return 변환된 Comment 객체, 변환 실패 시 null
     */
    private Comment unmarshallComment(Map<String, AttributeValue> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            return null;
        }
        try {
            Comment comment = new Comment();
            comment.setCommentId(getStringValue(attributeMap, "commentId"));
            comment.setPostId(getStringValue(attributeMap, "postId"));
            comment.setUserId(getStringValue(attributeMap, "userId"));
            comment.setContent(getStringValue(attributeMap, "content"));
            comment.setParentCommentId(getStringValue(attributeMap, "parentCommentId"));
            comment.setLikesCount(getLongValue(attributeMap, "likesCount", 0L));
            comment.setIsActive(getBooleanValue(attributeMap, "isActive", true));
            comment.setCreatedAt(getInstantValue(attributeMap, "createdAt"));
            comment.setUpdatedAt(getInstantValue(attributeMap, "updatedAt"));
            return comment;
        } catch (Exception e) {
            log.error("Failed to unmarshall Comment object from DynamoDB Stream record: {}", attributeMap, e);
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

    /**
     * AttributeValue에서 Long 값 추출 (기본값 지원)
     */
    private Long getLongValue(Map<String, AttributeValue> attributeMap, String key, Long defaultValue) {
        AttributeValue value = attributeMap.get(key);
        if (value != null && value.getN() != null) {
            try {
                return Long.parseLong(value.getN());
            } catch (NumberFormatException e) {
                log.warn("Failed to parse Long value for key {}: {}", key, value.getN());
            }
        }
        return defaultValue;
    }

    /**
     * AttributeValue에서 Boolean 값 추출 (기본값 지원)
     */
    private Boolean getBooleanValue(Map<String, AttributeValue> attributeMap, String key, Boolean defaultValue) {
        AttributeValue value = attributeMap.get(key);
        if (value != null && value.getBOOL() != null) {
            return value.getBOOL();
        }
        return defaultValue;
    }

    /**
     * AttributeValue에서 Instant 값 추출 (ISO-8601 문자열에서 변환)
     */
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

    @Override
    protected String getPrimaryKeyName() {
        return "commentId";
    }
}