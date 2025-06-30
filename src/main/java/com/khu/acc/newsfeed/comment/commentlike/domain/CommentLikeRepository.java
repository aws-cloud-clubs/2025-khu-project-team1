package com.khu.acc.newsfeed.comment.commentlike.domain;

import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface CommentLikeRepository extends DynamoDBPagingAndSortingRepository<CommentLike, String> {

    CommentLike save(CommentLike commentLike);

    Optional<CommentLike> findById(String commentLikeId);

    // 특정 댓글과 사용자의 좋아요 조회 (중복 체크용)
    Optional<CommentLike> findByCommentIdAndUserId(
            @Param("commentId") String commentId, 
            @Param("userId") String userId);

    // 특정 댓글의 모든 좋아요 조회 (시간순 정렬)
    List<CommentLike> findByCommentIdOrderByCreatedAtDesc(@Param("commentId") String commentId);

    // 특정 사용자가 누른 모든 댓글 좋아요 조회 (시간순 정렬)
    List<CommentLike> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);

    // 특정 댓글의 좋아요 수 카운트
    Long countByCommentId(@Param("commentId") String commentId);

    // 특정 사용자가 누른 댓글 좋아요 수 카운트
    Long countByUserId(@Param("userId") String userId);

    // 특정 댓글과 사용자의 좋아요 삭제
    void deleteByCommentIdAndUserId(
            @Param("commentId") String commentId, 
            @Param("userId") String userId);

    // 특정 댓글의 모든 좋아요 삭제 (댓글 삭제 시)
    void deleteByCommentId(@Param("commentId") String commentId);

    // 특정 시간 이후의 댓글 좋아요들 (최근 활동)
    List<CommentLike> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("createdAt") Instant createdAt);

    // 특정 댓글의 최근 좋아요들
    List<CommentLike> findByCommentIdAndCreatedAtAfterOrderByCreatedAtDesc(
            @Param("commentId") String commentId, 
            @Param("createdAt") Instant createdAt);

    // 댓글 좋아요 존재 여부 확인
    boolean existsByCommentIdAndUserId(
            @Param("commentId") String commentId, 
            @Param("userId") String userId);
    
    // ===== 커서 기반 최적화 쿠리 =====
    
    // 댓글별 좋아요 - 커서 기반 (최신순)
    List<CommentLike> findByCommentIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("commentId") String commentId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 댓글별 좋아요 - 커서 기반 (처음 페이지) - List 반환
    List<CommentLike> findTopByCommentIdOrderByCreatedAtDesc(
            @Param("commentId") String commentId, Pageable pageable);
    
    // 사용자별 댓글 좋아요 - 커서 기반 (최신순)
    List<CommentLike> findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("userId") String userId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 사용자별 댓글 좋아요 - 커서 기반 (처음 페이지) - List 반환
    List<CommentLike> findTopByUserIdOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);
}