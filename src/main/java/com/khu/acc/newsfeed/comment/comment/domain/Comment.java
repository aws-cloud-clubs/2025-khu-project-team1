package com.khu.acc.newsfeed.comment.comment.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.khu.acc.newsfeed.common.model.InstantConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;

import java.time.Instant;
import java.util.UUID;

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
    @Builder.Default
    private Long likesCount = 0L;

    @DynamoDBAttribute(attributeName = "isActive")
    @Builder.Default
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
    
    // Static factory methods
    public static Comment create(String commentId, String postId, String userId, String content,
                                String userNickname, String userProfileImage) {
        Comment comment = new Comment();
        comment.commentId = commentId;
        comment.postId = postId;
        comment.userId = userId;
        comment.content = content;
        comment.parentCommentId = null;
        comment.likesCount = 0L;
        comment.isActive = true;
        comment.createdAt = Instant.now();
        comment.updatedAt = Instant.now();
        return comment;
    }
    
    public static Comment createReply(String commentId, String postId, String userId, String content,
                                     String parentCommentId, String userNickname, String userProfileImage) {
        Comment comment = create(commentId, postId, userId, content, userNickname, userProfileImage);
        comment.parentCommentId = parentCommentId;
        return comment;
    }
    
    public static Comment of(String postId, String userId, String content, 
                            String userNickname, String userProfileImage) {
        String commentId = java.util.UUID.randomUUID().toString();
        return create(commentId, postId, userId, content, userNickname, userProfileImage);
    }
    
    public static Comment replyOf(String postId, String userId, String content, String parentCommentId,
                                 String userNickname, String userProfileImage) {
        String commentId = UUID.randomUUID().toString();
        return createReply(commentId, postId, userId, content, parentCommentId, userNickname, userProfileImage);
    }
    
    
    // 댓글 삭제 처리 (소프트 삭제)
    @DynamoDbIgnore
    public void markAsDeleted() {
        this.content = "삭제된 댓글입니다.";
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    
    // 댓글 수정
    @DynamoDbIgnore
    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedAt = Instant.now();
    }

    @DynamoDbIgnore
    public boolean isActive() {
        return isActive;
    }
}