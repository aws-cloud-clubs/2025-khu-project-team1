package com.khu.acc.newsfeed.comment.commentlike.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "CommentLikes")
public class CommentLike {

    @DynamoDBHashKey(attributeName = "commentLikeId")
    private String commentLikeId; // Format: commentId#userId

    @DynamoDBAttribute(attributeName = "commentId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "CommentLikesIndex")
    private String commentId;

    @DynamoDBAttribute(attributeName = "userId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "UserCommentLikesIndex")
    private String userId;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexNames = {"CommentLikesIndex", "UserCommentLikesIndex"})
    private Instant createdAt;

    // Static factory methods
    public static CommentLike create(String commentLikeId, String commentId, String userId) {
        CommentLike like = new CommentLike();
        like.commentLikeId = commentLikeId;
        like.commentId = commentId;
        like.userId = userId;
        like.createdAt = Instant.now();
        return like;
    }

    public static CommentLike of(String commentId, String userId) {
        return create(generateCommentLikeId(commentId, userId), commentId, userId);
    }

    // Generate composite key
    @DynamoDBIgnore
    public static String generateCommentLikeId(String commentId, String userId) {
        return commentId + "#" + userId;
    }

    // Check if this is a specific user's like
    @DynamoDBIgnore
    public boolean isOwnedBy(String userId) {
        return this.userId != null && this.userId.equals(userId);
    }
}