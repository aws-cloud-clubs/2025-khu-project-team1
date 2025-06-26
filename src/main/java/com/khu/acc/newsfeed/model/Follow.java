package com.khu.acc.newsfeed.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import org.springframework.data.annotation.Id;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "Follows")
public class Follow {

    @DynamoDBHashKey(attributeName = "followId")
    private String followId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "FollowerIndex", attributeName = "followerId")
    private String followerId;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "FolloweeIndex", attributeName = "followeeId")
    private String followeeId;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant createdAt;
}
