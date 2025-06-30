package com.khu.acc.newsfeed.post.postlike.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Likes")
public class Like {

    @DynamoDBHashKey(attributeName = "likeId")
    private String likeId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "PostIndex", attributeName = "postId")
    private String postId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserIndex", attributeName = "userId")
    private String userId;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant createdAt;

    // Static factory methods
    public static Like create(String likeId, String postId, String userId) {
        Like like = new Like();
        like.likeId = likeId;
        like.postId = postId;
        like.userId = userId;
        like.createdAt = Instant.now();
        return like;
    }

    public static Like of(String postId, String userId) {
        return create(generateLikeId(postId, userId), postId, userId);
    }

    private static String generateLikeId(String postId, String userId) {
        return "like_" + postId + "_" + userId;
    }

    @DynamoDBIgnore
    public boolean isFromUser(String userId) {
        return this.userId != null && this.userId.equals(userId);
    }

    @DynamoDBIgnore
    public boolean isForPost(String postId) {
        return this.postId != null && this.postId.equals(postId);
    }
}