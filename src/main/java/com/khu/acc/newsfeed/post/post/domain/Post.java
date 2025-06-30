package com.khu.acc.newsfeed.post.post.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import com.khu.acc.newsfeed.post.post.application.command.PostCreateCommand;
import com.khu.acc.newsfeed.post.post.application.command.PostUpdateCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Posts")
public class Post {

    @DynamoDBHashKey(attributeName = "postId")
    private String postId;

    @DynamoDBAttribute(attributeName = "userId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserPostsIndex")
    private String userId;

    @DynamoDBAttribute(attributeName = "content")
    private String content;

    @DynamoDBAttribute(attributeName = "imageUrls")
    private List<String> imageUrls;

    @DynamoDBAttribute(attributeName = "likesCount")
    private Long likesCount = 0L;

    @DynamoDBAttribute(attributeName = "commentsCount")
    private Long commentsCount = 0L;

    @DynamoDBAttribute(attributeName = "tags")
    private Set<String> tags;

    @DynamoDBAttribute(attributeName = "location")
    private String location;

    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "UserPostsIndex")
    private Instant createdAt;

    @DynamoDBAttribute(attributeName = "updatedAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant updatedAt;

    @DynamoDBIgnore
    public void incrementLikesCount() {
        this.likesCount = (this.likesCount == null ? 0 : this.likesCount) + 1;
    }

    @DynamoDBIgnore
    public void decrementLikesCount() {
        this.likesCount = Math.max(0, (this.likesCount == null ? 0 : this.likesCount) - 1);
    }

    @DynamoDBIgnore
    public void incrementCommentsCount() {
        this.commentsCount = (this.commentsCount == null ? 0 : this.commentsCount) + 1;
    }

    @DynamoDBIgnore
    public void decrementCommentsCount() {
        this.commentsCount = Math.max(0, (this.commentsCount == null ? 0 : this.commentsCount) - 1);
    }

    // Static factory methods
    
    /**
     * PostCreateCommand로부터 Post 엔티티를 생성
     */
    public static Post from(PostCreateCommand command) {
        Post post = new Post();
        post.postId = generatePostId();
        post.userId = command.getUserId();
        post.content = command.getContent();
        post.imageUrls = command.getImageUrls() != null ? command.getImageUrls() : new java.util.ArrayList<>();
        post.tags = command.getTags() != null ? command.getTags() : new java.util.HashSet<>();
        post.location = command.getLocation();
        post.likesCount = 0L;
        post.commentsCount = 0L;
        post.isActive = true;
        post.createdAt = Instant.now();
        post.updatedAt = Instant.now();
        return post;
    }
    
    /**
     * PostUpdateCommand로 포스트 업데이트
     */
    public void updateFrom(PostUpdateCommand command) {
        if (command.getContent() != null) {
            this.content = command.getContent();
        }
        if (command.getTags() != null) {
            this.tags = command.getTags();
        }
        if (command.getLocation() != null) {
            this.location = command.getLocation();
        }
        this.updatedAt = Instant.now();
    }
    
    /**
     * PostId 생성 (UUID 기반)
     */
    private static String generatePostId() {
        return "post_" + UUID.randomUUID().toString().replace("-", "");
    }

    public boolean isActive(){
        return isActive;
    }

}