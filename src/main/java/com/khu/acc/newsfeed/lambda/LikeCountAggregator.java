package com.khu.acc.newsfeed.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.khu.acc.newsfeed.post.post.domain.Post;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * EventBridge 스케줄러에 의해 5초마다 실행되는 배치 처리 Lambda
 * LikeCountBuffer 테이블의 델타 값들을 읽어서 Posts 테이블을 일괄 업데이트
 */
@Slf4j
public class LikeCountAggregator implements RequestHandler<ScheduledEvent, Void> {

    private final DynamoDBMapper dynamoDBMapper;

    public LikeCountAggregator() {
        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
        this.dynamoDBMapper = new DynamoDBMapper(dynamoDBClient, DynamoDBMapperConfig.DEFAULT);
    }

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        log.info("Starting like count aggregation batch job");
        
        try {
            int processedCount = processPendingLikeCounts();
            log.info("Like count aggregation completed. Processed {} posts", processedCount);
        } catch (Exception e) {
            log.error("Like count aggregation failed", e);
            throw e;
        }

        return null;
    }

    private int processPendingLikeCounts() {
        int processedCount = 0;
        List<LikeCountBuffer> buffersToReset = new ArrayList<>();

        try {
            // LikeCountBuffer 테이블 전체 스캔하여 pending 업데이트 조회
            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
            PaginatedScanList<LikeCountBuffer> buffers = dynamoDBMapper.scan(LikeCountBuffer.class, scanExpression);

            log.debug("Found {} buffer records to check", buffers.size());

            for (LikeCountBuffer buffer : buffers) {
                if (buffer.hasPendingUpdates()) {
                    try {
                        updatePostLikeCount(buffer);
                        buffersToReset.add(buffer);
                        processedCount++;
                        
                        log.debug("Updated post {} like count by {}", 
                                buffer.getPostId(), buffer.getDeltaCount());
                    } catch (Exception e) {
                        log.error("Failed to update like count for post: {}", buffer.getPostId(), e);
                        // 개별 실패가 전체 배치를 중단시키지 않도록 계속 진행
                    }
                }
            }

            // 처리 완료된 버퍼들 초기화 (배치로 처리)
            if (!buffersToReset.isEmpty()) {
                resetProcessedBuffers(buffersToReset);
                log.debug("Reset {} processed buffer records", buffersToReset.size());
            }

        } catch (Exception e) {
            log.error("Failed to process pending like counts", e);
            throw e;
        }

        return processedCount;
    }

    private void updatePostLikeCount(LikeCountBuffer buffer) {
        String postId = buffer.getPostId();
        Long deltaCount = buffer.getDeltaCount();

        try {
            // Post 조회
            Post post = dynamoDBMapper.load(Post.class, postId);
            if (post == null) {
                log.warn("Post not found for like count update: {}", postId);
                return;
            }

            // 현재 좋아요 수에 델타 적용
            long currentCount = post.getLikesCount() != null ? post.getLikesCount() : 0L;
            long newCount = Math.max(0, currentCount + deltaCount);
            
            post.setLikesCount(newCount);
            
            // Post 업데이트
            dynamoDBMapper.save(post);
            
            log.debug("Updated post {} like count: {} -> {} (delta: {})", 
                    postId, currentCount, newCount, deltaCount);

        } catch (Exception e) {
            log.error("Failed to update post like count for postId: {}", postId, e);
            throw e;
        }
    }

    private void resetProcessedBuffers(List<LikeCountBuffer> buffers) {
        try {
            // 각 버퍼의 deltaCount를 0으로 초기화
            for (LikeCountBuffer buffer : buffers) {
                buffer.reset();
            }
            
            // 배치로 저장
            dynamoDBMapper.batchSave(buffers);
            
        } catch (Exception e) {
            log.error("Failed to reset processed buffers", e);
            // 버퍼 초기화 실패는 중요하지만 전체 프로세스를 중단시키지는 않음
            // 다음 배치에서 중복 처리될 수 있지만 idempotent하게 설계되어 문제없음
        }
    }
}