package com.khu.acc.newsfeed.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Comments")
public class Comment {

    @DynamoDBHashKey(attributeName = "commentId")
    private String commentId;

    @DynamoDBAttribute(attributeName = "postId")
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "PostCommentsIndex")
    private String postId;

    @DynamoDBAttribute(attributeName = "userId")
    private String userId;

    @DynamoDBAttribute(attributeName = "content")
    private String content;

    @DynamoDBAttribute(attributeName = "parentCommentId")
    private String parentCommentId; // For nested comments

    @DynamoDBAttribute(attributeName = "likesCount")
    private Long likesCount = 0L;

    @DynamoDBAttribute(attributeName = "isActive")
    private Boolean isActive = true;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "PostCommentsIndex")
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
    public boolean isReply() {
        return parentCommentId != null && !parentCommentId.isEmpty();
    }
}