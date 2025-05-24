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
@DynamoDBTable(tableName = "Follows")
public class Follow {

    @DynamoDBHashKey(attributeName = "followerId")
    private String followerId;

    @DynamoDBRangeKey(attributeName = "followeeId")
    private String followeeId;

    @DynamoDBAttribute(attributeName = "createdAt")
    @DynamoDBTypeConverted(converter = InstantConverter.class)
    private Instant createdAt;

    // GSI for reverse lookup (followers of a user)
    @DynamoDBIndexHashKey(globalSecondaryIndexName = "FolloweeIndex")
    public String getFolloweeId() {
        return followeeId;
    }

    @DynamoDBIndexRangeKey(globalSecondaryIndexName = "FolloweeIndex")
    public String getFollowerId() {
        return followerId;
    }
}
