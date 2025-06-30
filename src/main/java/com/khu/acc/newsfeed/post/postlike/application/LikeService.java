package com.khu.acc.newsfeed.post.postlike.application;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.common.exception.like.LikeAlreadyExistsException;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import com.khu.acc.newsfeed.post.postlike.domain.Like;
import com.khu.acc.newsfeed.post.postlike.domain.LikeRepository;
import com.khu.acc.newsfeed.common.service.CursorPaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CursorPaginationService paginationService;

    public Like likePost(String postId, String userId) {
        // 포스트 존재 확인
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 자신의 포스트 좋아요 방지
        if (post.getUserId().equals(userId)) {
            throw LikeAlreadyExistsException.selfLikeNotAllowed();
        }

        // Idempotent 처리 - 이미 좋아요가 있으면 기존 것 반환
        Like existingLike = likeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (existingLike != null) {
            log.debug("User {} already liked post {}, returning existing like", userId, postId);
            return existingLike;
        }

        // 새 좋아요 생성
        Like newLike = Like.of(postId, userId);
        Like savedLike = likeRepository.save(newLike);

        log.info("User {} liked post {}", userId, postId);
        return savedLike;
    }

    public void unlikePost(String postId, String userId) {
        // Idempotent 처리 - 좋아요가 없으면 아무것도 하지 않음
        Like existingLike = likeRepository.findByPostIdAndUserId(postId, userId).orElse(null);
        if (existingLike == null) {
            log.debug("User {} has not liked post {}, nothing to remove", userId, postId);
            return;
        }

        // 좋아요 삭제
        likeRepository.deleteByPostIdAndUserId(postId, userId);

        log.info("User {} unliked post {}", userId, postId);
    }

    public ScrollResponse<Like> getPostLikes(String postId, ScrollRequest scrollRequest) {
        // 포스트 존재 확인
        postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 모든 좋아요 조회
        List<Like> allLikes = likeRepository.findByPostIdOrderByCreatedAtDesc(postId);
        return buildScrollResponse(allLikes, scrollRequest);
    }

    public ScrollResponse<Like> getUserLikes(String userId, ScrollRequest scrollRequest) {
        // 사용자가 누른 모든 좋아요 조회
        List<Like> allLikes = likeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return buildScrollResponse(allLikes, scrollRequest);
    }

    public boolean hasUserLikedPost(String postId, String userId) {
        return likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    public Long getPostLikesCount(String postId) {
        // Post에서 직접 조회 (배치 집계된 값)
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        return post.getLikesCount();
    }

    public Long getUserLikesCount(String userId) {
        return likeRepository.countByUserId(userId);
    }

    // 포스트 삭제 시 관련 좋아요 모두 삭제
    public void deleteAllLikesForPost(String postId) {
        likeRepository.deleteByPostId(postId);
        log.info("Deleted all likes for post {}", postId);
    }

    // 최근 좋아요 활동 조회
    public List<Like> getRecentLikes(Instant since) {
        return likeRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since);
    }

    // 특정 포스트의 최근 좋아요들
    public List<Like> getRecentPostLikes(String postId, Instant since) {
        return likeRepository.findByPostIdAndCreatedAtAfterOrderByCreatedAtDesc(postId, since);
    }

    // 최적화된 페이징 메서드들
    
    /**
     * 포스트별 좋아요 조회 - 최적화된 버전
     */
    public ScrollResponse<Like> getPostLikesOptimized(String postId, ScrollRequest scrollRequest) {
        // 포스트 존재 확인
        postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        return paginationService.paginateWithQuery(
                request -> executePostLikesQuery(postId, request),
                scrollRequest,
                Like::getLikeId
        );
    }
    
    /**
     * 사용자별 좋아요 조회 - 최적화된 버전
     */
    public ScrollResponse<Like> getUserLikesOptimized(String userId, ScrollRequest scrollRequest) {
        return paginationService.paginateWithQuery(
                request -> executeUserLikesQuery(userId, request),
                scrollRequest,
                Like::getLikeId
        );
    }
    
    // 실제 DB 쿼리 실행 메서드들
    private List<Like> executePostLikesQuery(String postId, ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return likeRepository.findTopByPostIdOrderByCreatedAtDesc(postId, pageable);
        } else {
            Instant cursorTime = parseCursorToInstant(request.getCursor());
            return likeRepository.findByPostIdAndCreatedAtLessThanOrderByCreatedAtDesc(
                    postId, cursorTime, pageable);
        }
    }
    
    private List<Like> executeUserLikesQuery(String userId, ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return likeRepository.findTopByUserIdOrderByCreatedAtDesc(userId, pageable);
        } else {
            Instant cursorTime = parseCursorToInstant(request.getCursor());
            return likeRepository.findByUserIdAndCreatedAtLessThanOrderByCreatedAtDesc(
                    userId, cursorTime, pageable);
        }
    }
    
    // 커서 파싱 유틸리티
    private Instant parseCursorToInstant(String cursor) {
        try {
            return Instant.parse(cursor);
        } catch (Exception e) {
            try {
                Like like = likeRepository.findByLikeId(cursor)
                        .orElse(null);
                if (like != null) {
                    return like.getCreatedAt();
                }
            } catch (Exception ex) {
                log.warn("Failed to parse cursor: {}, using current time", cursor);
            }
            return Instant.now();
        }
    }
    
    // 기존 메모리 기반 메서드 (호환성 유지)
    private ScrollResponse<Like> buildScrollResponse(List<Like> allLikes, ScrollRequest scrollRequest) {
        return paginationService.paginate(allLikes, scrollRequest, Like::getLikeId);
    }
}
