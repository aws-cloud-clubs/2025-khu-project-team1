package com.khu.acc.newsfeed.newsfeed.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * NewsFeedItem의 복합 키 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewsFeedItemKey implements Serializable {
    
    @DynamoDBHashKey(attributeName = "userId")
    private String userId;
    
    @DynamoDBRangeKey(attributeName = "sortKey")
    private String sortKey;
}