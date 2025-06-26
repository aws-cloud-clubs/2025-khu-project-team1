package com.khu.acc.newsfeed.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Users")
public class User {

    @DynamoDBHashKey(attributeName = "userId")
    private String userId;

    @DynamoDBAttribute(attributeName = "email")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "EmailIndex")
    private String email;

    @DynamoDBAttribute(attributeName = "username")
    private String username;

    @DynamoDBAttribute(attributeName = "displayName")
    private String displayName;

    @DynamoDBAttribute(attributeName = "profileImageUrl")
    private String profileImageUrl;

    @DynamoDBAttribute(attributeName = "bio")
    private String bio;

    @DynamoDBAttribute(attributeName = "followersCount")
    private Long followersCount = 0L;

    @DynamoDBAttribute(attributeName = "followingCount")
    private Long followingCount = 0L;

    @DynamoDBAttribute(attributeName = "postsCount")
    private Long postsCount = 0L;

    @DynamoDBAttribute(attributeName = "interests")
    private Set<String> interests;

    @DynamoDBAttribute(attributeName = "isActive")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "isActiveIndex")
    private String isActive = "true";

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant createdAt;

    @DynamoDBAttribute(attributeName = "updatedAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant updatedAt;

    @DynamoDBIgnore
    public void incrementFollowersCount() {
        this.followersCount = (this.followersCount == null ? 0 : this.followersCount) + 1;
    }

    @DynamoDBIgnore
    public void decrementFollowersCount() {
        this.followersCount = Math.max(0, (this.followersCount == null ? 0 : this.followersCount) - 1);
    }

    @DynamoDBIgnore
    public void incrementFollowingCount() {
        this.followingCount = (this.followingCount == null ? 0 : this.followingCount) + 1;
    }

    @DynamoDBIgnore
    public void decrementFollowingCount() {
        this.followingCount = Math.max(0, (this.followingCount == null ? 0 : this.followingCount) - 1);
    }

    @DynamoDBIgnore
    public void incrementPostsCount() {
        this.postsCount = (this.postsCount == null ? 0 : this.postsCount) + 1;
    }

    @DynamoDBIgnore
    public void decrementPostsCount() {
        this.postsCount = Math.max(0, (this.postsCount == null ? 0 : this.postsCount) - 1);
    }
}