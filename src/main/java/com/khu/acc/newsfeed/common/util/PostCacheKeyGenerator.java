package com.khu.acc.newsfeed.common.util;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import org.springframework.stereotype.Component;

/**
 * 포스트 관련 캐시 키 생성 유틸리티
 * 일관된 캐시 키 형식과 네이밍 컨벤션 제공
 */
@Component
public class PostCacheKeyGenerator {

    // 캐시 키 구분자
    private static final String DELIMITER = "-";
    
    // 캐시 키 접두사
    private static final String POST_PREFIX = "post";
    private static final String USER_FEED_PREFIX = "userFeed";
    private static final String TRENDING_PREFIX = "trending";
    private static final String TAG_PREFIX = "tag";
    private static final String SEARCH_PREFIX = "search";

    /**
     * 단일 포스트 캐시 키 생성
     */
    public String generatePostKey(String postId) {
        return POST_PREFIX + DELIMITER + postId;
    }

    /**
     * 사용자 피드 캐시 키 생성
     */
    public String generateUserFeedKey(String userId, ScrollRequest scrollRequest) {
        return USER_FEED_PREFIX + DELIMITER + userId + DELIMITER + 
               generateScrollKey(scrollRequest);
    }

    /**
     * 트렌딩 포스트 캐시 키 생성
     */
    public String generateTrendingKey(ScrollRequest scrollRequest) {
        return TRENDING_PREFIX + DELIMITER + generateScrollKey(scrollRequest);
    }

    /**
     * 태그별 포스트 캐시 키 생성
     */
    public String generateTagKey(String tag, ScrollRequest scrollRequest) {
        return TAG_PREFIX + DELIMITER + sanitizeTag(tag) + DELIMITER + 
               generateScrollKey(scrollRequest);
    }

    /**
     * 검색 결과 캐시 키 생성
     */
    public String generateSearchKey(String content, ScrollRequest scrollRequest) {
        return SEARCH_PREFIX + DELIMITER + sanitizeContent(content) + DELIMITER + 
               generateScrollKey(scrollRequest);
    }

    /**
     * 사용자별 포스트 수 캐시 키 생성
     */
    public String generateUserPostCountKey(String userId) {
        return "userPostCount" + DELIMITER + userId;
    }

    /**
     * 최적화된 사용자 피드 캐시 키 생성
     */
    public String generateOptimizedUserFeedKey(String userId, ScrollRequest scrollRequest) {
        return "userFeedOptimized" + DELIMITER + userId + DELIMITER + 
               generateScrollKey(scrollRequest);
    }

    /**
     * 최적화된 트렌딩 포스트 캐시 키 생성
     */
    public String generateOptimizedTrendingKey(ScrollRequest scrollRequest) {
        return "trendingOptimized" + DELIMITER + generateScrollKey(scrollRequest);
    }

    /**
     * 스크롤 요청 기반 키 생성
     */
    private String generateScrollKey(ScrollRequest scrollRequest) {
        if (scrollRequest == null) {
            return "default";
        }
        
        String cursor = scrollRequest.getCursor();
        int limit = scrollRequest.getLimit();
        
        // null 커서는 "null"로 처리
        String cursorPart = (cursor == null || cursor.isEmpty()) ? "null" : sanitizeCursor(cursor);
        
        return cursorPart + DELIMITER + limit;
    }

    /**
     * 커서 문자열 정리 (캐시 키에 안전한 형태로 변환)
     */
    private String sanitizeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return "null";
        }
        
        // 특수 문자를 언더스코어로 변경
        return cursor.replaceAll("[^a-zA-Z0-9#]", "_");
    }

    /**
     * 태그 문자열 정리
     */
    private String sanitizeTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            return "empty";
        }
        
        // 공백과 특수문자를 언더스코어로 변경
        return tag.replaceAll("[^a-zA-Z0-9가-힣]", "_");
    }

    /**
     * 검색 컨텐츠 문자열 정리
     */
    private String sanitizeContent(String content) {
        if (content == null || content.isEmpty()) {
            return "empty";
        }
        
        // 길이 제한 및 특수문자 정리
        String sanitized = content.replaceAll("[^a-zA-Z0-9가-힣\\s]", "_");
        return sanitized.length() > 50 ? sanitized.substring(0, 50) : sanitized;
    }

    /**
     * 캐시 키 패턴 생성 (와일드카드 삭제용)
     */
    public String generateUserFeedPattern(String userId) {
        return USER_FEED_PREFIX + DELIMITER + userId + "*";
    }

    /**
     * 모든 트렌딩 캐시 패턴 생성
     */
    public String generateTrendingPattern() {
        return TRENDING_PREFIX + "*";
    }

    /**
     * 특정 포스트 관련 모든 캐시 패턴 생성
     */
    public String generatePostRelatedPattern(String postId) {
        return "*" + postId + "*";
    }

    /**
     * 복합 캐시 키 검증
     */
    public boolean isValidCacheKey(String key) {
        return key != null && 
               !key.isEmpty() && 
               key.length() <= 250 && // Redis 키 길이 제한
               !key.contains(" "); // 공백 없음
    }
}