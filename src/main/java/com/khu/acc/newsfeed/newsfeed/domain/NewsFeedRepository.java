package com.khu.acc.newsfeed.newsfeed.domain;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 뉴스피드 데이터 접근 리포지토리
 */
@EnableScan
public interface NewsFeedRepository extends DynamoDBPagingAndSortingRepository<NewsFeedItem, NewsFeedItemKey> {
    
    // 기본 CRUD
    NewsFeedItem save(NewsFeedItem item);
    
    Optional<NewsFeedItem> findByUserIdAndSortKey(@Param("userId") String userId, @Param("sortKey") String sortKey);
    
    // 사용자 피드 조회 (시간순 정렬)
    List<NewsFeedItem> findByUserIdAndIsActiveTrueOrderBySortKey(@Param("userId") String userId);
    
    Page<NewsFeedItem> findByUserIdAndIsActiveTrueOrderBySortKey(
            @Param("userId") String userId, Pageable pageable);
    
    // 커서 기반 페이징을 위한 조회
    List<NewsFeedItem> findByUserIdAndIsActiveTrueAndSortKeyGreaterThanOrderBySortKey(
            @Param("userId") String userId, @Param("sortKey") String cursor, Pageable pageable);
    
    // 특정 포스트의 모든 피드 아이템 조회 (포스트 삭제 시 사용)
    List<NewsFeedItem> findByPostIdAndIsActiveTrue(@Param("postId") String postId);
    
    // 특정 작성자의 포스트들에 대한 피드 아이템 조회 (언팔로우 시 사용)
    List<NewsFeedItem> findByUserIdAndAuthorIdAndIsActiveTrueOrderBySortKey(
            @Param("userId") String userId, @Param("authorId") String authorId);
    
    // 특정 시간 이후의 피드 아이템 조회
    List<NewsFeedItem> findByUserIdAndIsActiveTrueAndFeedCreatedAtAfterOrderBySortKey(
            @Param("userId") String userId, @Param("feedCreatedAt") Instant since);
    
    // 특정 포스트에 대한 사용자별 피드 아이템 조회
    Optional<NewsFeedItem> findByUserIdAndPostIdAndIsActiveTrue(
            @Param("userId") String userId, @Param("postId") String postId);
    
    // 사용자 피드 아이템 수 조회
    Long countByUserIdAndIsActiveTrue(@Param("userId") String userId);
    
    // 특정 작성자의 포스트에 대한 피드 아이템 수 조회
    Long countByUserIdAndAuthorIdAndIsActiveTrue(
            @Param("userId") String userId, @Param("authorId") String authorId);
    
    // 배치 삭제를 위한 메서드들
    
    /**
     * 특정 포스트의 모든 피드 아이템 비활성화 (포스트 삭제 시)
     */
    List<NewsFeedItem> findByPostId(@Param("postId") String postId);
    
    /**
     * 특정 사용자의 특정 작성자에 대한 모든 피드 아이템 조회 (언팔로우 시)
     */
    List<NewsFeedItem> findByUserIdAndAuthorId(@Param("userId") String userId, @Param("authorId") String authorId);
    
    /**
     * 활성 피드 아이템만 삭제 (실제 DynamoDB DELETE)
     */
    void deleteByUserIdAndPostIdAndIsActiveTrue(@Param("userId") String userId, @Param("postId") String postId);
    
    /**
     * 모든 피드 아이템 삭제 (포스트 완전 삭제 시)
     */
    void deleteByPostId(@Param("postId") String postId);
    
    /**
     * 사용자의 특정 작성자 피드 아이템 모두 삭제 (언팔로우 시)
     */
    void deleteByUserIdAndAuthorId(@Param("userId") String userId, @Param("authorId") String authorId);
    
    // 최적화된 쿼리들
    
    /**
     * 최신 N개 피드 아이템 조회
     */
    List<NewsFeedItem> findTopByUserIdAndIsActiveTrueOrderBySortKey(
            @Param("userId") String userId, Pageable pageable);
    
    /**
     * 특정 시간 범위의 피드 아이템 조회
     */
    List<NewsFeedItem> findByUserIdAndIsActiveTrueAndCreatedAtBetweenOrderBySortKey(
            @Param("userId") String userId, 
            @Param("startTime") Instant startTime, 
            @Param("endTime") Instant endTime, 
            Pageable pageable);
}