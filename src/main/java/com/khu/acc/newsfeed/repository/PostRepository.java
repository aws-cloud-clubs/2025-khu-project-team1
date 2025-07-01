// PostRepository 업데이트
package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Post;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

@EnableScan
public interface PostRepository extends DynamoDBPagingAndSortingRepository<Post, String> {

    Post save(Post post);

    // 기본 조회
    List<Post> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Post> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    // 사용자별 포스트 조회
    List<Post> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId);
    Page<Post> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    // 다중 사용자 포스트 조회 (팔로잉 피드용)
    List<Post> findByUserIdInAndCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("userIds") List<String> userIds,
            @Param("createdAt") Instant createdAt,
            Pageable pageable);

    // 트렌딩 포스트 (참여도 기준)
    List<Post> findByCreatedAtAfterAndIsActiveTrueOrderByLikesCountDescCommentsCountDesc(
            @Param("createdAt") Instant createdAt, Pageable pageable);

    // 태그별 포스트 조회
    List<Post> findByTagsContainingAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("tag") String tag, Pageable pageable);

    Page<Post> findByTagsContainingAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("tag") String tag, Pageable pageable);

    // 인기 포스트 (좋아요 수 기준)
    List<Post> findByIsActiveTrueOrderByLikesCountDescCreatedAtDesc(Pageable pageable);

    // 최근 포스트
    List<Post> findByCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("createdAt") Instant createdAt, Pageable pageable);

    // 콘텐츠 검색
    List<Post> findByContentContainingAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("content") String content, Pageable pageable);

    // 통계용 카운트
    Long countByUserIdAndIsActiveTrue(@Param("userId") String userId);
    Long countByUserIdInAndCreatedAtAfterAndIsActiveTrue(
            @Param("userIds") List<String> userIds, @Param("createdAt") Instant createdAt);
    Long countByTagsContainingAndIsActiveTrue(@Param("tag") String tag);

    // 위치별 포스트
    List<Post> findByLocationContainingAndIsActiveTrueOrderByCreatedAtDesc(
            @Param("location") String location, Pageable pageable);
}

// LikeRepository 업데이트
package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Like;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface LikeRepository extends DynamoDBPagingAndSortingRepository<Like, String> {

    // 포스트별 좋아요 조회
    List<Like> findByPostIdOrderByCreatedAtDesc(@Param("postId") String postId);
    Page<Like> findByPostIdOrderByCreatedAtDesc(@Param("postId") String postId, Pageable pageable);

    // 사용자별 좋아요 조회
    List<Like> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    Page<Like> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    // 특정 사용자의 특정 포스트 좋아요 여부 확인
    Optional<Like> findByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);

    // 좋아요 존재 여부 확인
    boolean existsByPostIdAndUserId(@Param("postId") String postId, @Param("userId") String userId);

    // 포스트별 좋아요 수 카운트
    Long countByPostId(@Param("postId") String postId);

    // 사용자별 좋아요 수 카운트
    Long countByUserId(@Param("userId") String userId);

    // 최근 좋아요 조회 (개인화용)
    List<Like> findByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("createdAt") Instant createdAt);

    // 최근 인기 포스트 (좋아요 기준)
    List<Like> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("createdAt") Instant createdAt);
}

// FollowRepository 업데이트
package com.khu.acc.newsfeed.repository;

import com.khu.acc.newsfeed.model.Follow;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@EnableScan
public interface FollowRepository extends DynamoDBPagingAndSortingRepository<Follow, String> {

    // 팔로워 조회
    List<Follow> findByFolloweeIdOrderByCreatedAtDesc(@Param("followeeId") String followeeId);
    Page<Follow> findByFolloweeIdOrderByCreatedAtDesc(@Param("followeeId") String followeeId, Pageable pageable);

    // 팔로잉 조회
    List<Follow> findByFollowerIdOrderByCreatedAtDesc(@Param("followerId") String followerId);
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(@Param("followerId") String followerId, Pageable pageable);

    // 팔로우 관계 확인
    Optional<Follow> findByFollowerIdAndFolloweeId(@Param("followerId") String followerId, @Param("followeeId") String followeeId);
    boolean existsByFollowerIdAndFolloweeId(@Param("followerId") String followerId, @Param("followeeId") String followeeId);

    // 팔로워/팔로잉 수 카운트
    Long countByFolloweeId(@Param("followeeId") String followeeId);
    Long countByFollowerId(@Param("followerId") String followerId);

    // 최근 팔로우
    List<Follow> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("createdAt") Instant createdAt);

    // 상호 팔로우 확인용
    List<Follow> findByFollowerIdAndFolloweeIdIn(@Param("followerId") String followerId, @Param("followeeIds") List<String> followeeIds);
}

// CommentRepository 업데이트 (기존 코드에 추가)
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

    // 기존 메서드들...
    List<Comment> findByPostIdAndIsActiveTrueOrderByCreatedAtAsc(@Param("postId") String postId);
    Page<Comment> findByPostIdAndIsActiveTrueOrderByCreatedAtAsc(@Param("postId") String postId, Pageable pageable);
    List<Comment> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId);
    Page<Comment> findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(@Param("userId") String userId, Pageable pageable);

    // 개인화를 위한 추가 메서드들
    List<Comment> findByUserIdAndCreatedAtAfter(@Param("userId") String userId, @Param("createdAt") Instant createdAt);
    List<Comment> findByUserIdAndCreatedAtAfterAndIsActiveTrue(@Param("userId") String userId, @Param("createdAt") Instant createdAt);

    // 최근 활발한 포스트 찾기용
    List<Comment> findByCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(@Param("createdAt") Instant createdAt);

    // 사용자별 최근 댓글 통계
    Long countByUserIdAndCreatedAtAfterAndIsActiveTrue(@Param("userId") String userId, @Param("createdAt") Instant createdAt);

    // 포스트별 최근 댓글 수
    Long countByPostIdAndCreatedAtAfterAndIsActiveTrue(@Param("postId") String postId, @Param("createdAt") Instant createdAt);
}