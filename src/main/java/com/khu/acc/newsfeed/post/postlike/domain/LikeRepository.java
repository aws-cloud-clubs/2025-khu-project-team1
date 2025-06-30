package com.khu.acc.newsfeed.post.postlike.domain;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface LikeRepository extends DynamoDBPagingAndSortingRepository<Like, String> {

    Like save(Like like);

    Optional<Like> findByLikeId(String likeId);

    // 특정 포스트와 사용자의 좋아요 조회 (중복 체크용)
    Optional<Like> findByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);

    // 특정 포스트의 모든 좋아요 조회 (시간순 정렬)
    List<Like> findByPostIdOrderByCreatedAtDesc(@Param("postId") String postId);

    // 특정 사용자가 누른 모든 좋아요 조회 (시간순 정렬)
    List<Like> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);


    // 특정 사용자가 누른 좋아요 수 카운트
    Long countByUserId(@Param("userId") String userId);

    // 특정 포스트와 사용자의 좋아요 삭제
    void deleteByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);

    // 특정 포스트의 모든 좋아요 삭제 (포스트 삭제 시)
    void deleteByPostId(@Param("postId") String postId);

    // 특정 시간 이후의 좋아요들 (최근 활동)
    List<Like> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("createdAt") Instant createdAt);

    // 특정 포스트의 최근 좋아요들
    List<Like> findByPostIdAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("postId") String postId, @Param("createdAt") Instant createdAt);

    // 좋아요 존재 여부 확인
    boolean existsByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);
    
    // ===== 커서 기반 최적화 쿠리 =====
    
    // 포스트별 좋아요 - 커서 기반 (최신순)
    List<Like> findByPostIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("postId") String postId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 포스트별 좋아요 - 커서 기반 (처음 페이지) - List 반환
    List<Like> findTopByPostIdOrderByCreatedAtDesc(@Param("postId") String postId, Pageable pageable);
    
    // 사용자별 좋아요 - 커서 기반 (최신순)
    List<Like> findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("userId") String userId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 사용자별 좋아요 - 커서 기반 (처음 페이지) - List 반환
    List<Like> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);
}
