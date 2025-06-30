package com.khu.acc.newsfeed.newsfeed.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

/**
 * 뉴스피드 아이템 모델
 * 팔로우한 사용자들의 포스트를 개인화된 피드로 제공
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "NewsFeed")
public class NewsFeedItem {

    @Id
    @DynamoDBHashKey(attributeName = "userId")
    private String userId; // 피드를 받을 사용자 ID

    @DynamoDBRangeKey(attributeName = "sortKey")
    private String sortKey; // timestamp#postId 형태로 시간순 정렬

    @DynamoDBAttribute(attributeName = "postId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "PostIndex")
    private String postId; // 포스트 ID

    @DynamoDBAttribute(attributeName = "authorId")
    private String authorId; // 포스트 작성자 ID

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "PostIndex")
    private Instant createdAt; // 포스트 생성 시간

    @DynamoDBAttribute(attributeName = "feedCreatedAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant feedCreatedAt; // 피드 아이템 생성 시간

    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true; // 활성 상태

    @DynamoDBAttribute(attributeName = "feedItemId")
    private String feedItemId; // 고유 피드 아이템 ID

    // Static factory methods

    /**
     * 포스트 생성 시 팔로워들의 피드에 추가할 아이템 생성
     */
    public static NewsFeedItem forPost(String userId, String postId, String authorId, Instant postCreatedAt) {
        NewsFeedItem item = new NewsFeedItem();
        item.userId = userId;
        item.postId = postId;
        item.authorId = authorId;
        item.createdAt = postCreatedAt;
        item.feedCreatedAt = Instant.now();
        item.isActive = true;
        item.feedItemId = generateFeedItemId();
        item.sortKey = generateSortKey(postCreatedAt, postId);
        return item;
    }

    /**
     * 새로운 팔로우 시 기존 포스트들을 피드에 추가할 아이템 생성
     */
    public static NewsFeedItem forExistingPost(String userId, String postId, String authorId, Instant postCreatedAt) {
        NewsFeedItem item = forPost(userId, postId, authorId, postCreatedAt);
        item.feedCreatedAt = Instant.now(); // 피드 추가 시간은 현재 시간
        return item;
    }

    /**
     * 시간순 정렬을 위한 sortKey 생성
     * 최신 포스트가 앞에 오도록 역순 정렬
     */
    private static String generateSortKey(Instant createdAt, String postId) {
        // 역순 정렬을 위해 Long.MAX_VALUE에서 timestamp를 뺌
        long reverseTimestamp = Long.MAX_VALUE - createdAt.getEpochSecond();
        return String.format("%019d#%s", reverseTimestamp, postId);
    }

    /**
     * 고유 피드 아이템 ID 생성
     */
    private static String generateFeedItemId() {
        return "feed_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 피드 아이템 비활성화 (소프트 삭제)
     */
    public void deactivate() {
        this.isActive = false;
        this.feedCreatedAt = Instant.now(); // 수정 시간 업데이트
    }

    /**
     * 포스트 작성자인지 확인
     */
    public boolean isAuthoredBy(String authorId) {
        return this.authorId != null && this.authorId.equals(authorId);
    }

    /**
     * 특정 시간 이후에 생성된 피드 아이템인지 확인
     */
    public boolean isCreatedAfter(Instant timestamp) {
        return this.feedCreatedAt != null && this.feedCreatedAt.isAfter(timestamp);
    }

    /**
     * 포스트가 특정 시간 이후에 생성되었는지 확인
     */
    public boolean isPostCreatedAfter(Instant timestamp) {
        return this.createdAt != null && this.createdAt.isAfter(timestamp);
    }

    /**
     * DynamoDB 복합 키 가져오기
     */
    public NewsFeedItemKey getKey() {
        return new NewsFeedItemKey(userId, sortKey);
    }
}