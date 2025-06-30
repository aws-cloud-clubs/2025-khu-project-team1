package com.khu.acc.newsfeed.newsfeed.application;

import com.khu.acc.newsfeed.newsfeed.domain.NewsFeedItem;
import com.khu.acc.newsfeed.newsfeed.domain.NewsFeedRepository;
import com.khu.acc.newsfeed.post.post.interfaces.dto.PostResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import com.khu.acc.newsfeed.common.service.CursorPaginationService;
import com.khu.acc.newsfeed.user.application.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 뉴스피드 서비스
 * 사용자별 개인화된 피드 제공
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsFeedService {

    private final NewsFeedRepository newsFeedRepository;
    private final PostRepository postRepository;
    private final CursorPaginationService paginationService;
    private final UserValidator userValidator;

    /**
     * 사용자의 뉴스피드 조회 (메인 피드)
     */
    @Cacheable(value = "newsFeed", key = "#userId + '-' + #scrollRequest.cursor + '-' + #scrollRequest.limit")
    public ScrollResponse<PostResponse> getUserNewsFeed(String userId, ScrollRequest scrollRequest) {
        // 사용자 존재 확인
        userValidator.validateUserExists(userId);

        // 뉴스피드 아이템 조회
        ScrollResponse<NewsFeedItem> feedItems = paginationService.paginateWithQuery(
                request -> queryNewsFeedItems(userId, request),
                scrollRequest,
                NewsFeedItem::getFeedItemId
        );

        // 포스트 정보 조회 및 조립
        List<PostResponse> postResponses = assemblePostResponses(feedItems.getContent());

        return ScrollResponse.of(
                postResponses,
                feedItems.getNextCursor(),
                feedItems.getPreviousCursor(),
                feedItems.isHasNext(),
                feedItems.isHasPrevious()
        );
    }

    /**
     * 개인화된 피드 조회 (알고리즘 기반)
     * 현재는 기본 피드와 동일, 향후 ML 기반 개인화 확장 가능
     */
    @Cacheable(value = "personalizedFeed", key = "#userId + '-' + #scrollRequest.cursor + '-' + #scrollRequest.limit")
    public ScrollResponse<PostResponse> getPersonalizedFeed(String userId, ScrollRequest scrollRequest) {
        log.debug("Getting personalized feed for user: {}", userId);
        
        // 현재는 기본 뉴스피드와 동일하게 처리
        // 향후 확장 포인트:
        // 1. 사용자 관심사 분석
        // 2. 좋아요/댓글 패턴 분석
        // 3. 머신러닝 기반 추천
        return getUserNewsFeed(userId, scrollRequest);
    }

    /**
     * 뉴스피드 새로고침
     * 캐시 무효화 및 최신 데이터 반환
     */
    @Transactional
    public ScrollResponse<PostResponse> refreshNewsFeed(String userId, ScrollRequest scrollRequest) {
        log.info("Refreshing news feed for user: {}", userId);
        
        // 캐시 무효화는 @CacheEvict으로 처리하지 않고 수동으로 처리
        // 실제 구현에서는 Redis 등의 캐시 매니저를 통해 특정 키 삭제
        
        return getUserNewsFeed(userId, scrollRequest);
    }

    /**
     * 사용자 피드 통계 조회
     */
    public NewsFeedStats getUserFeedStats(String userId) {
        // 사용자 존재 확인
        userValidator.validateUserExists(userId);

        Long totalItems = newsFeedRepository.countByUserIdAndIsActiveTrue(userId);
        
        // 최근 24시간 내 새로운 아이템 수
        Instant since = Instant.now().minusSeconds(24 * 60 * 60);
        List<NewsFeedItem> recentItems = newsFeedRepository
                .findByUserIdAndIsActiveTrueAndFeedCreatedAtAfterOrderBySortKey(userId, since);
        
        return NewsFeedStats.of(totalItems, recentItems.size());
    }

    /**
     * 특정 작성자의 포스트가 사용자 피드에 있는지 확인
     */
    public boolean hasAuthorPostsInFeed(String userId, String authorId) {
        Long count = newsFeedRepository.countByUserIdAndAuthorIdAndIsActiveTrue(userId, authorId);
        return count > 0;
    }

    // Private helper methods

    private List<NewsFeedItem> queryNewsFeedItems(String userId, ScrollRequest request) {
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return newsFeedRepository.findTopByUserIdAndIsActiveTrueOrderBySortKey(
                    userId, 
                    org.springframework.data.domain.PageRequest.of(0, request.getLimit())
            );
        } else {
            return newsFeedRepository.findByUserIdAndIsActiveTrueAndSortKeyGreaterThanOrderBySortKey(
                    userId, 
                    request.getCursor(),
                    org.springframework.data.domain.PageRequest.of(0, request.getLimit())
            );
        }
    }

    private List<PostResponse> assemblePostResponses(List<NewsFeedItem> feedItems) {
        if (feedItems.isEmpty()) {
            return List.of();
        }

        // 포스트 ID 목록 추출
        List<String> postIds = feedItems.stream()
                .map(NewsFeedItem::getPostId)
                .toList();

        // 배치로 포스트 조회
        List<Post> posts = postRepository.findByPostIdInAndIsActiveTrueOrderByCreatedAtDesc(postIds);

        // 포스트 ID를 키로 하는 Map 생성
        Map<String, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, Function.identity()));

        // 피드 아이템 순서대로 PostResponse 생성
        return feedItems.stream()
                .map(feedItem -> postMap.get(feedItem.getPostId()))
                .filter(post -> post != null) // null 포스트 필터링
                .map(PostResponse::from)
                .toList();
    }

    /**
     * 뉴스피드 통계 DTO
     */
    public static class NewsFeedStats {
        private final Long totalItems;
        private final Integer recentItems;

        private NewsFeedStats(Long totalItems, Integer recentItems) {
            this.totalItems = totalItems;
            this.recentItems = recentItems;
        }

        public static NewsFeedStats of(Long totalItems, Integer recentItems) {
            return new NewsFeedStats(totalItems, recentItems);
        }

        public Long getTotalItems() { return totalItems; }
        public Integer getRecentItems() { return recentItems; }
    }
}