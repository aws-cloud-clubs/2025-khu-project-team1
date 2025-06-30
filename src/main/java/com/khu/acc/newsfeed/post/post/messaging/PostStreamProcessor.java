package com.khu.acc.newsfeed.post.post.messaging;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.khu.acc.newsfeed.common.stream.DynamoDBStreamHandler;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.messaging.event.PostCreatedEvent;
import com.khu.acc.newsfeed.post.post.messaging.event.PostDeletedEvent;
import com.khu.acc.newsfeed.post.post.messaging.event.PostUpdatedEvent;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

/**
 * Posts 테이블의 DynamoDB Streams 이벤트 처리
 * 타입-세이프한 Post 객체로 변환하여 처리
 * 실제 Post 엔티티 변경 시 SNS로 이벤트 발행 (팬아웃은 별도 Lambda에서 처리)
 */
@Slf4j
public class PostStreamProcessor extends DynamoDBStreamHandler {

    private static final DynamoDBMapper DYNAMO_DB_MAPPER = new DynamoDBMapper(
            AmazonDynamoDBClientBuilder.defaultClient(),
            DynamoDBMapperConfig.DEFAULT
    );


    @Override
    protected void processRecord(DynamodbEvent.DynamodbStreamRecord record, Context context) {
        logRecordInfo(record);

        // 실제 Post 엔티티만 처리 (EVENT# 아이템은 무시)
        if (isEventItem(record.getDynamodb().getKeys())) {
            log.debug("Skipping EVENT# item");
            return;
        }

        String eventName = record.getEventName();

        switch (eventName) {
            case "INSERT":
                Post createdPost = unmarshallPost(record.getDynamodb().getNewImage());
                if (createdPost != null) {
                    handlePostCreated(createdPost);
                }
                break;
            case "MODIFY":
                Post newPost = unmarshallPost(record.getDynamodb().getNewImage());
                Post oldPost = unmarshallPost(record.getDynamodb().getOldImage());
                if (newPost != null && oldPost != null) {
                    handlePostModified(newPost, oldPost);
                }
                break;
            case "REMOVE":
                Post deletedPost = unmarshallPost(record.getDynamodb().getOldImage());
                if (deletedPost != null) {
                    handlePostDeleted(deletedPost);
                }
                break;
            default:
                log.warn("Unknown event type: {}", eventName);
        }
    }

    /**
     * DynamoDB Stream 이벤트의 AttributeValue Map을 Post 객체로 변환
     * 각 필드를 안전하게 추출하여 Post 객체를 생성합니다.
     */
    private Post unmarshallPost(Map<String, AttributeValue> attributeMap) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            log.debug("AttributeMap is null or empty");
            return null;
        }

        try {
            Post post = new Post();
            
            // Required fields
            post.setPostId(getStringValue(attributeMap, "postId"));
            post.setUserId(getStringValue(attributeMap, "userId"));
            
            // Content fields
            post.setContent(getStringValue(attributeMap, "content"));
            post.setLocation(getStringValue(attributeMap, "location"));
            
            // Numeric fields
            post.setLikesCount(getLongValue(attributeMap, "likesCount", 0L));
            post.setCommentsCount(getLongValue(attributeMap, "commentsCount", 0L));
            
            // Boolean fields
            post.setIsActive(getBooleanValue(attributeMap, "isActive", true));
            
            // Timestamp fields
            post.setCreatedAt(getInstantValue(attributeMap, "createdAt"));
            post.setUpdatedAt(getInstantValue(attributeMap, "updatedAt"));
            
            // Collection fields
            post.setImageUrls(getStringListValue(attributeMap, "imageUrls"));
            post.setTags(getStringSetValue(attributeMap, "tags"));
            
            log.debug("Successfully unmarshalled Post: {}", post.getPostId());
            return post;
            
        } catch (Exception e) {
            log.error("Failed to unmarshall Post object from DynamoDB Stream record: {}", attributeMap, e);
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
    
    /**
     * AttributeValue에서 String List 값 추출
     */
    private java.util.List<String> getStringListValue(Map<String, AttributeValue> attributeMap, String key) {
        AttributeValue value = attributeMap.get(key);
        if (value != null && value.getSS() != null) {
            return new java.util.ArrayList<>(value.getSS());
        }
        return new java.util.ArrayList<>();
    }
    
    /**
     * AttributeValue에서 String Set 값 추출
     */
    private java.util.Set<String> getStringSetValue(Map<String, AttributeValue> attributeMap, String key) {
        AttributeValue value = attributeMap.get(key);
        if (value != null && value.getSS() != null) {
            return new java.util.HashSet<>(value.getSS());
        }
        return new java.util.HashSet<>();
    }

    /**
     * 타입-세이프한 Post 객체를 사용한 포스트 생성 이벤트 처리
     */
    private void handlePostCreated(Post post) {
        // isActive가 true인 경우만 처리 - 타입 안전!
        if (!post.isActive()) {
            log.debug("Skipping inactive post: {}", post.getPostId());
            return;
        }

        // 타입-세이프한 이벤트 생성 - 컴파일 시점 타입 체크!
        PostCreatedEvent event = new PostCreatedEvent(
                post.getPostId(),
                post.getUserId(),
                post.getCreatedAt()
        );
        
        publishToSns(event);
        
        log.info("Published PostCreatedEvent for postId: {}, userId: {}", 
                post.getPostId(), post.getUserId());
    }

    private void handlePostModified(Post newPost, Post oldPost) {
        String postId = newPost.getPostId();
        String userId = newPost.getUserId();

        Boolean oldIsActive = oldPost.getIsActive();
        Boolean newIsActive = newPost.getIsActive();
        
        if ((oldIsActive != null && oldIsActive) && (newIsActive == null || !newIsActive)) {
            PostDeletedEvent event = new PostDeletedEvent(postId, userId, Instant.now());
            publishToSns(event);
            log.info("Published PostDeletedEvent for deactivated post: {}", postId);
        } else if (newIsActive != null && newIsActive) {
            PostUpdatedEvent event = new PostUpdatedEvent(
                    newPost.getPostId(),
                    newPost.getUserId(),
                    newPost.getUpdatedAt()
            );
            publishToSns(event);
            log.info("Published PostUpdatedEvent for postId: {}", postId);
        }
    }

    private void handlePostDeleted(Post post) {
        PostDeletedEvent event = new PostDeletedEvent(
                post.getPostId(),
                post.getUserId(), 
                Instant.now()
        );
        publishToSns(event);
        
        log.info("Published PostDeletedEvent for hard deleted post: {}", post.getPostId());
    }

    @Override
    protected String getPrimaryKeyName() {
        return "postId";
    }
}