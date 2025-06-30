package com.khu.acc.newsfeed.comment.comment.domain;


import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface CommentRepository extends DynamoDBPagingAndSortingRepository<Comment, String> {

    Comment save(Comment comment);

    Optional<Comment> findById(String commentId);

    // 특정 포스트의 댓글들 (시간순 정렬)
    List<Comment> findByPostIdAndIsActiveTrueOrderByCreatedAtAsc(@Param("postId") String postId);

    Page<Comment> findByPostIdAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("postId") String postId, Pageable pageable);

    // 특정 사용자의 댓글들
    List<Comment> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId);

    Page<Comment> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);

    // 특정 댓글의 대댓글들
    List<Comment> findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("parentCommentId") String parentCommentId);

    // 최상위 댓글들만 (대댓글 제외)
    List<Comment> findByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("postId") String postId);

    Page<Comment> findByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("postId") String postId, Pageable pageable);

    // 포스트별 댓글 수 카운트
    Long countByPostIdAndIsActiveTrue(@Param("postId") String postId);

    // 사용자별 댓글 수 카운트
    Long countByUserIdAndIsActiveTrue(@Param("userId") String userId);

    // 특정 댓글의 대댓글 수 카운트
    Long countByParentCommentIdAndIsActiveTrue(@Param("parentCommentId") String parentCommentId);

    // 내용으로 댓글 검색
    List<Comment> findByContentContainingAndIsActiveTrue(@Param("content") String content);

    // 최근 댓글들
    List<Comment> findByCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("createdAt") Instant createdAt);

    // 인기 댓글들 (좋아요 수 기준)
    List<Comment> findByPostIdAndIsActiveTrueOrderByLikesCountDescCreatedAtAsc(
            @Param("postId") String postId);

    Page<Comment> findByPostIdAndIsActiveTrueOrderByLikesCountDescCreatedAtAsc(
            @Param("postId") String postId, Pageable pageable);
    
    // ===== 커서 기반 최적화 쿠리 =====
    
    // 포스트별 댓글 - 커서 기반 (오래된 순)
    List<Comment> findByPostIdAndParentCommentIdIsNullAndIsActiveTrueAndCreatedAtGreaterThanOrderByCreatedAtAsc(
            @Param("postId") String postId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 포스트별 댓글 - 커서 기반 (처음 페이지) - List 반환
    List<Comment> findTopByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("postId") String postId, Pageable pageable);
    
    // 댓글별 대댓글 - 커서 기반 (오래된 순)
    List<Comment> findByParentCommentIdAndIsActiveTrueAndCreatedAtGreaterThanOrderByCreatedAtAsc(
            @Param("parentCommentId") String parentCommentId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 댓글별 대댓글 - 커서 기반 (처음 페이지) - List 반환  
    List<Comment> findTopByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(
            @Param("parentCommentId") String parentCommentId, Pageable pageable);
    
    // 사용자별 댓글 - 커서 기반 (최신순)
    List<Comment> findByUserIdAndIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
            @Param("userId") String userId, @Param("createdAt") Instant cursor, Pageable pageable);
    
    // 사용자별 댓글 - 커서 기반 (처음 페이지) - List 반환
    List<Comment> findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("userId") String userId, Pageable pageable);
}
