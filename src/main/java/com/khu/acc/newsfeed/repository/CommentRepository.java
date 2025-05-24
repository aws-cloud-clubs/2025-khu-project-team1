package com.khu.acc.newsfeed.repository;


import com.khu.acc.newsfeed.model.Comment;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

@EnableScan
public interface CommentRepository extends DynamoDBPagingAndSortingRepository<Comment, String> {

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
}
