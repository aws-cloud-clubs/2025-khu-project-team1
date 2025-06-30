package com.khu.acc.newsfeed.newsfeed.messaging;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.khu.acc.newsfeed.post.post.messaging.event.PostCreatedEvent;
import com.khu.acc.newsfeed.post.post.messaging.event.PostDeletedEvent;
import com.khu.acc.newsfeed.post.post.messaging.event.PostUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SNS 이벤트를 받아서 NewsFeed 팬아웃 처리
 * Post 생성/수정/삭제 시 팔로워들의 피드에 반영
 */
@Slf4j
public class NewsFeedFanoutLambda implements RequestHandler<SNSEvent, Void> {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    public NewsFeedFanoutLambda() {
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.AP_NORTHEAST_2)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Void handleRequest(SNSEvent event, Context context) {
        log.info("Processing {} SNS records", event.getRecords().size());

        for (SNSEvent.SNSRecord record : event.getRecords()) {
            try {
                processSNSRecord(record);
            } catch (Exception e) {
                log.error("Failed to process SNS record: {}", record.getSNS().getMessageId(), e);
                // 개별 레코드 실패가 전체 배치를 실패시키지 않도록 처리
                // 추후 Dead Letter Queue 추가 가능
            }
        }

        return null;
    }

    private void processSNSRecord(SNSEvent.SNSRecord record) throws Exception {
        SNSEvent.SNS sns = record.getSNS();
        String eventType = getEventType(sns);
        String messageBody = sns.getMessage();

        log.info("Processing event type: {}", eventType);

        switch (eventType) {
            case "POST_CREATED":
                handlePostCreated(objectMapper.readValue(messageBody, PostCreatedEvent.class));
                break;
            case "POST_UPDATED":
                handlePostUpdated(objectMapper.readValue(messageBody, PostUpdatedEvent.class));
                break;
            case "POST_DELETED":
                handlePostDeleted(objectMapper.readValue(messageBody, PostDeletedEvent.class));
                break;
            default:
                log.warn("Unknown event type: {}", eventType);
        }
    }

    private String getEventType(SNSEvent.SNS sns) {
        Map<String, SNSEvent.MessageAttribute> attributes = sns.getMessageAttributes();
        if (attributes != null && attributes.containsKey("eventType")) {
            return attributes.get("eventType").getValue();
        }
        return "UNKNOWN";
    }

    private void handlePostCreated(PostCreatedEvent event) {
        log.info("Handling PostCreatedEvent: postId={}, userId={}", event.getPostId(), event.getAuthorId());

        // 1. 팔로워 목록 조회
        List<String> followers = getFollowers(event.getAuthorId());
        log.info("Found {} followers for user {}", followers.size(), event.getAuthorId());

        // 2. 각 팔로워의 피드에 아이템 추가
        for (String followerId : followers) {
            try {
                addToNewsFeed(followerId, event);
                log.debug("Added post {} to {}'s feed", event.getPostId(), followerId);
            } catch (Exception e) {
                log.error("Failed to add post {} to {}'s feed", event.getPostId(), followerId, e);
            }
        }

        log.info("Completed fanout for post {} to {} followers", event.getPostId(), followers.size());
    }

    private void handlePostUpdated(PostUpdatedEvent event) {
        log.info("Handling PostUpdatedEvent: postId={}", event.getPostId());
        
        // 포스트 수정 시에는 기존 피드 아이템 업데이트 또는 새로 추가
        // 현재는 로깅만 수행 (향후 확장 가능)
        // 실제로는 NewsFeed 테이블에서 해당 포스트를 찾아서 업데이트하거나
        // 필요시 새로운 팬아웃 수행
    }

    private void handlePostDeleted(PostDeletedEvent event) {
        log.info("Handling PostDeletedEvent: postId={}", event.getPostId());

        try {
            // 모든 사용자의 피드에서 해당 포스트 제거
            removeFromAllFeeds(event.getPostId());
            log.info("Removed post {} from all feeds", event.getPostId());
        } catch (Exception e) {
            log.error("Failed to remove post {} from feeds", event.getPostId(), e);
        }
    }

    private List<String> getFollowers(String userId) {
        try {
            // Follows 테이블에서 해당 사용자를 팔로우하는 사용자들 조회
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName("Follows")
                    .indexName("FolloweeIndex")
                    .keyConditionExpression("followeeId = :followeeId")
                    .filterExpression("isActive = :isActive")
                    .expressionAttributeValues(Map.of(
                            ":followeeId", AttributeValue.builder().s(userId).build(),
                            ":isActive", AttributeValue.builder().bool(true).build()
                    ))
                    .projectionExpression("followerId")
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);
            
            return response.items().stream()
                    .map(item -> item.get("followerId").s())
                    .toList();

        } catch (Exception e) {
            log.error("Failed to get followers for user {}", userId, e);
            return List.of();
        }
    }

    private void addToNewsFeed(String userId, PostCreatedEvent event) {
        try {
            // sortKey 생성 (시간순 정렬을 위해)
            long reverseTimestamp = Long.MAX_VALUE - event.getCreatedAt().getEpochSecond();
            String sortKey = String.format("%019d#%s", reverseTimestamp, event.getPostId());

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("userId", AttributeValue.builder().s(userId).build());
            item.put("sortKey", AttributeValue.builder().s(sortKey).build());
            item.put("postId", AttributeValue.builder().s(event.getPostId()).build());
            item.put("authorId", AttributeValue.builder().s(event.getAuthorId()).build());
            item.put("createdAt", AttributeValue.builder().s(event.getCreatedAt().toString()).build());
            item.put("feedCreatedAt", AttributeValue.builder().s(Instant.now().toString()).build());
            item.put("isActive", AttributeValue.builder().bool(true).build());
            item.put("feedItemId", AttributeValue.builder().s(generateFeedItemId()).build());

            PutItemRequest putRequest = PutItemRequest.builder()
                    .tableName("NewsFeed")
                    .item(item)
                    .conditionExpression("attribute_not_exists(userId) AND attribute_not_exists(sortKey)")
                    .build();

            dynamoDbClient.putItem(putRequest);

        } catch (ConditionalCheckFailedException e) {
            log.debug("Feed item already exists for user {} and post {}", userId, event.getPostId());
        } catch (Exception e) {
            log.error("Failed to add feed item for user {} and post {}", userId, event.getPostId(), e);
            throw e;
        }
    }

    private void removeFromAllFeeds(String postId) {
        try {
            // PostIndex를 사용하여 해당 포스트의 모든 피드 아이템 조회
            QueryRequest queryRequest = QueryRequest.builder()
                    .tableName("NewsFeed")
                    .indexName("PostIndex")
                    .keyConditionExpression("postId = :postId")
                    .expressionAttributeValues(Map.of(
                            ":postId", AttributeValue.builder().s(postId).build()
                    ))
                    .build();

            QueryResponse response = dynamoDbClient.query(queryRequest);

            // 각 피드 아이템 삭제
            for (Map<String, AttributeValue> item : response.items()) {
                String userId = item.get("userId").s();
                String sortKey = item.get("sortKey").s();

                DeleteItemRequest deleteRequest = DeleteItemRequest.builder()
                        .tableName("NewsFeed")
                        .key(Map.of(
                                "userId", AttributeValue.builder().s(userId).build(),
                                "sortKey", AttributeValue.builder().s(sortKey).build()
                        ))
                        .build();

                dynamoDbClient.deleteItem(deleteRequest);
                log.debug("Removed feed item for user {} and post {}", userId, postId);
            }

        } catch (Exception e) {
            log.error("Failed to remove feed items for post {}", postId, e);
            throw e;
        }
    }

    private String generateFeedItemId() {
        return "feed_" + java.util.UUID.randomUUID().toString().replace("-", "");
    }
}