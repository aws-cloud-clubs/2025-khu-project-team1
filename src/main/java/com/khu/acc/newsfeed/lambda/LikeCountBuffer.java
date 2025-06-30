package com.khu.acc.newsfeed.lambda;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 좋아요 카운트 배치 처리를 위한 버퍼 테이블
 * 5초 배치 윈도우 동안 델타 값을 누적 저장
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamoDBTable(tableName = "LikeCountBuffer")
public class LikeCountBuffer {

    @DynamoDBHashKey(attributeName = "postId")
    private String postId;

    @DynamoDBAttribute(attributeName = "deltaCount")
    private Long deltaCount = 0L;

    @DynamoDBAttribute(attributeName = "lastUpdated")
    private String lastUpdated;

    @DynamoDBAttribute(attributeName = "version")
    private Long version = 1L;

    /**
     * 델타 값 증가
     */
    public void incrementDelta(int amount) {
        this.deltaCount = (this.deltaCount == null ? 0 : this.deltaCount) + amount;
        this.lastUpdated = Instant.now().toString();
        this.version = (this.version == null ? 1 : this.version) + 1;
    }

    /**
     * 델타 값 감소
     */
    public void decrementDelta(int amount) {
        this.deltaCount = (this.deltaCount == null ? 0 : this.deltaCount) - amount;
        this.lastUpdated = Instant.now().toString();
        this.version = (this.version == null ? 1 : this.version) + 1;
    }

    /**
     * 배치 처리 후 초기화
     */
    public void reset() {
        this.deltaCount = 0L;
        this.lastUpdated = Instant.now().toString();
        this.version = (this.version == null ? 1 : this.version) + 1;
    }

    /**
     * 처리할 델타가 있는지 확인
     */
    public boolean hasPendingUpdates() {
        return this.deltaCount != null && this.deltaCount != 0L;
    }
}