package com.khu.acc.newsfeed.post.post.domain;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface PostRepository extends DynamoDBPagingAndSortingRepository<Post, String> {
    
    Post save(Post post);
    
    Optional<Post> findByPostId(String postId);
    
    // 활성 포스트들 (좋아요 수 기준 정렬)
    List<Post> findByIsActiveTrueOrderByLikesCountDescCreatedAtDesc();
    
    // 특정 태그를 가진 포스트들
    List<Post> findByTagsContainingAndIsActiveTrueOrderByCreatedAtDesc(@Param("tag") String tag);

    
    // 내용으로 포스트 검색
    List<Post> findByContentContainingAndIsActiveTrueOrderByCreatedAtDesc(@Param("content") String content);
    
    // 특정 사용자의 포스트 수 카운트
    Long countByUserIdAndIsActiveTrue(@Param("userId") String userId);
    
    // ===== 커서 기반 최적화 쿠리 =====
    
    // 사용자별 포스트 - 커서 기반 (최신순)
    List<Post> findByUserIdAndIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("userId") String userId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 사용자별 포스트 - 커서 기반 (처음 페이지) - List 반환
    List<Post> findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);
    
    // 전체 포스트 - 커서 기반 (최신순)
    List<Post> findByIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 전체 포스트 - 처음 페이지 (List 반환)  
    List<Post> findTopByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // ===== PostQueryService에서 필요한 추가 메서드들 =====
    
    // 시간 범위 기반 포스트 조회
    List<Post> findByIsActiveTrueAndCreatedAtBetweenOrderByCreatedAtDesc(
            @Param("startTime") Instant startTime, @Param("endTime") Instant endTime, Pageable pageable);
    
    // 여러 사용자의 포스트 조회 (피드 생성용)
    List<Post> findByUserIdInAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("userIds") List<String> userIds, Pageable pageable);
    
    // 여러 사용자의 포스트 조회 - 커서 기반
    List<Post> findByUserIdInAndIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("userIds") List<String> userIds, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 특정 시간 이후 포스트 수 카운트
    Long countByUserIdAndIsActiveTrueAndCreatedAtGreaterThan(
            @Param("userId") String userId, @Param("createdAt") Instant since);

    List<Post> findByTagsInAndIsActiveTrueOrderByCreatedAtDesc(List<String> tags, Pageable pageable);

    boolean existsByPostIdAndIsActiveTrue(String postId);

    List<Post> findByIsActiveTrueAndCreatedAtBetweenOrderByLikesCountDescCreatedAtDesc(Instant startTime, Instant endTime, Pageable pageable);

    List<Post> findByPostIdInAndIsActiveTrueOrderByCreatedAtDesc(List<String> postIds);
}