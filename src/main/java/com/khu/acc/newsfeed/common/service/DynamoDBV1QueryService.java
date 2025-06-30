package com.khu.acc.newsfeed.common.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AWS SDK v1 DynamoDBMapper 기반 쿼리 서비스
 * 활성 아이템 필터링 등 공통 쿼리 기능 제공
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamoDBV1QueryService {

    private final DynamoDBMapper dynamoDBMapper;

    /**
     * 활성 아이템만 필터링하여 조회
     * 
     * @param hashKeyValue 파티션 키 값
     * @param indexName GSI 이름
     * @param entityClass 엔티티 클래스
     * @param scrollRequest 페이징 요청
     * @param descending 내림차순 여부
     * @return 활성 아이템 목록
     */
    public <T> List<T> queryActiveItems(
            String hashKeyValue,
            String indexName, 
            Class<T> entityClass,
            ScrollRequest scrollRequest,
            boolean descending) {
        
        try {
            T hashKeyObj = entityClass.getDeclaredConstructor().newInstance();
            
            // 리플렉션을 통해 해시키 설정 (간단한 구현)
            setHashKeyValue(hashKeyObj, hashKeyValue, entityClass);

            DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                    .withHashKeyValues(hashKeyObj)
                    .withIndexName(indexName)
                    .withConsistentRead(false)
                    .withScanIndexForward(!descending)
                    .withLimit(scrollRequest.getLimit())
                    .withFilterExpression("isActive = :isActive")
                    .withExpressionAttributeValues(createActiveFilterValues());

            if (scrollRequest.getCursor() != null && !scrollRequest.getCursor().isEmpty()) {
                Map<String, AttributeValue> exclusiveStartKey = createExclusiveStartKey(
                        hashKeyValue, scrollRequest.getCursor(), indexName);
                queryExpression.setExclusiveStartKey(exclusiveStartKey);
            }

            PaginatedQueryList<T> result = dynamoDBMapper.query(entityClass, queryExpression);
            return result.subList(0, Math.min(result.size(), scrollRequest.getLimit()));

        } catch (Exception e) {
            log.error("Failed to execute active items query for {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("DynamoDB active items query execution failed", e);
        }
    }

    /**
     * 활성 필터 속성값 생성
     */
    private Map<String, AttributeValue> createActiveFilterValues() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":isActive", new AttributeValue().withBOOL(true));
        return expressionAttributeValues;
    }

    /**
     * 해시키 값 설정 (간단한 구현)
     */
    private <T> void setHashKeyValue(T obj, String value, Class<T> entityClass) {
        try {
            // 주요 엔티티별 해시키 필드명 매핑
            String fieldName = getHashKeyFieldName(entityClass);
            if (fieldName != null) {
                entityClass.getDeclaredField(fieldName).set(obj, value);
            }
        } catch (Exception e) {
            log.warn("Failed to set hash key value for {}: {}", entityClass.getSimpleName(), e.getMessage());
        }
    }

    /**
     * 엔티티별 해시키 필드명 반환
     */
    private String getHashKeyFieldName(Class<?> entityClass) {
        String className = entityClass.getSimpleName();
        return switch (className) {
            case "Post" -> "postId";
            case "User" -> "userId";
            case "Comment" -> "commentId";
            case "Like" -> "likeId";
            case "Follow" -> "followId";
            case "NewsFeedItem" -> "userId";
            case "Notification" -> "notificationId";
            default -> "id";
        };
    }

    /**
     * ExclusiveStartKey 생성
     */
    private Map<String, AttributeValue> createExclusiveStartKey(
            String hashKeyValue, String cursor, String indexName) {
        
        Map<String, AttributeValue> exclusiveStartKey = new HashMap<>();
        
        if (indexName != null) {
            // GSI의 경우
            exclusiveStartKey.put("userId", new AttributeValue().withS(hashKeyValue));
            exclusiveStartKey.put("sortKey", new AttributeValue().withS(cursor));
        } else {
            // 주 테이블의 경우
            exclusiveStartKey.put("postId", new AttributeValue().withS(hashKeyValue));
            if (cursor.contains("#")) {
                String[] parts = cursor.split("#");
                exclusiveStartKey.put("createdAt", new AttributeValue().withS(parts[1]));
            }
        }
        
        return exclusiveStartKey;
    }

    /**
     * 시간 범위 기반 쿼리
     */
    public <T> List<T> queryTimeRange(
            String hashKeyValue,
            String startTime,
            String endTime,
            Class<T> entityClass,
            ScrollRequest scrollRequest) {
        
        try {
            T hashKeyObj = entityClass.getDeclaredConstructor().newInstance();
            setHashKeyValue(hashKeyObj, hashKeyValue, entityClass);

            DynamoDBQueryExpression<T> queryExpression = new DynamoDBQueryExpression<T>()
                    .withHashKeyValues(hashKeyObj)
                    .withConsistentRead(false)
                    .withScanIndexForward(false)
                    .withLimit(scrollRequest.getLimit())
                    .withFilterExpression("isActive = :isActive AND createdAt BETWEEN :startTime AND :endTime");

            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(":isActive", new AttributeValue().withBOOL(true));
            expressionAttributeValues.put(":startTime", new AttributeValue().withS(startTime));
            expressionAttributeValues.put(":endTime", new AttributeValue().withS(endTime));
            
            queryExpression.setExpressionAttributeValues(expressionAttributeValues);

            PaginatedQueryList<T> result = dynamoDBMapper.query(entityClass, queryExpression);
            return result.subList(0, Math.min(result.size(), scrollRequest.getLimit()));

        } catch (Exception e) {
            log.error("Failed to execute time range query for {}", entityClass.getSimpleName(), e);
            throw new RuntimeException("DynamoDB time range query execution failed", e);
        }
    }
}