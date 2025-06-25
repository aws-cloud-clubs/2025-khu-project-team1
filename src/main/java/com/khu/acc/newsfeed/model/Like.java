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
}