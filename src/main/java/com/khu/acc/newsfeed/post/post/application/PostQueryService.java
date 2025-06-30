package com.khu.acc.newsfeed.post.post.application;

import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.service.CursorPaginationService;
import com.khu.acc.newsfeed.common.util.CursorParser;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 포스트 조회 전용 서비스 (통합)
 * PostController와 외부 Aggregate 모두에서 사용하는 조회 서비스입니다.
 * 
 * 사용처:
 * - PostController: 사용자 인터페이스용 조회
 * - NewsFeedService: 피드 생성용 조회  
 * - NotificationService: 알림용 조회
 * - LikeService, CommentService: 포스트 존재 확인용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final CursorParser cursorParser;
    private final CursorPaginationService paginationService;

    /**
     * 사용자별 포스트 조회 (최적화된 커서 기반 페이징)
     */
    public List<Post> findUserPosts(String userId, ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return postRepository.findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);
        } else {
            Instant cursorTime = cursorParser.parseCursorToInstant(request.getCursor());
            return postRepository.findByUserIdAndIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
                    userId, cursorTime, pageable);
        }
    }

    /**
     * 전체 포스트 조회 (시간순)
     */
    public List<Post> findAllPosts(ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return postRepository.findTopByIsActiveTrueOrderByCreatedAtDesc(pageable);
        } else {
            Instant cursorTime = cursorParser.parseCursorToInstant(request.getCursor());
            return postRepository.findByIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
                    cursorTime, pageable);
        }
    }

    /**
     * 인기 포스트 조회 (좋아요 수 기준)
     * 메모리 기반 정렬이 필요한 경우
     */
    public List<Post> findTrendingPosts() {
        return postRepository.findByIsActiveTrueOrderByLikesCountDescCreatedAtDesc();
    }

    /**
     * 태그별 포스트 조회
     */
    public List<Post> findPostsByTag(String tag) {
        return postRepository.findByTagsContainingAndIsActiveTrueOrderByCreatedAtDesc(tag);
    }

    /**
     * 컨텐츠 검색
     */
    public List<Post> searchPostsByContent(String content) {
        return postRepository.findByContentContainingAndIsActiveTrueOrderByCreatedAtDesc(content);
    }

    /**
     * 최적화된 사용자 포스트 조회
     */
    public List<Post> findUserPostsOptimized(String userId, ScrollRequest request) {
        log.debug("Optimized user posts query for userId: {}, cursor: {}", userId, request.getCursor());
        return findUserPosts(userId, request);
    }

    /**
     * 시간 범위 기반 포스트 조회
     */
    public List<Post> findPostsInTimeRange(Instant startTime, Instant endTime, ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return postRepository.findByIsActiveTrueAndCreatedAtBetweenOrderByCreatedAtDesc(
                    startTime, endTime, pageable);
        } else {
            Instant cursorTime = cursorParser.parseCursorToInstant(request.getCursor());
            // 커서 시간이 범위 내에 있는지 확인 후 조회
            Instant effectiveEndTime = cursorTime.isBefore(endTime) ? cursorTime : endTime;
            return postRepository.findByIsActiveTrueAndCreatedAtBetweenOrderByCreatedAtDesc(
                    startTime, effectiveEndTime, pageable);
        }
    }

    /**
     * 특정 사용자들의 포스트 조회 (피드 생성용)
     */
    public List<Post> findPostsByUserIds(List<String> userIds, ScrollRequest request) {
        Pageable pageable = PageRequest.of(0, request.getLimit());
        
        if (request.getCursor() == null || request.getCursor().isEmpty()) {
            return postRepository.findByUserIdInAndIsActiveTrueOrderByCreatedAtDesc(userIds, pageable);
        } else {
            Instant cursorTime = cursorParser.parseCursorToInstant(request.getCursor());
            return postRepository.findByUserIdInAndIsActiveTrueAndCreatedAtLessThanOrderByCreatedAtDesc(
                    userIds, cursorTime, pageable);
        }
    }

    /**
     * 최신 포스트 개수 조회 (특정 시간 이후)
     */
    public long countRecentPosts(String userId, Instant since) {
        return postRepository.countByUserIdAndIsActiveTrueAndCreatedAtGreaterThan(userId, since);
    }

    /**
     * 사용자의 총 포스트 수 조회
     */
    public long countUserPosts(String userId) {
        return postRepository.countByUserIdAndIsActiveTrue(userId);
    }

    // === 외부 Aggregate 전용 메서드들 ===

    /**
     * 포스트 ID로 포스트 조회 (외부 aggregate용)
     * NewsFeedService에서 포스트 존재 확인 등에 사용
     * 
     * @param postId 포스트 ID
     * @return 포스트 (활성 상태만)
     */
    public Post findPostById(String postId) {
        log.debug("Finding post by id for external aggregate: postId={}", postId);
        return postRepository.findByPostId(postId)
                .filter(Post::getIsActive)
                .orElse(null);
    }

    /**
     * 여러 포스트 ID로 포스트들을 일괄 조회
     * NewsFeedService에서 피드 구성 시 사용
     * 
     * @param postIds 포스트 ID 목록
     * @return 활성 포스트 목록
     */
    public List<Post> findPostsByIds(List<String> postIds) {
        log.debug("Finding posts by ids for external aggregate: count={}", postIds.size());
        return postRepository.findByPostIdInAndIsActiveTrueOrderByCreatedAtDesc(postIds);
    }

    /**
     * 사용자의 최신 포스트를 제한된 개수로 조회
     * 알림 서비스에서 사용자의 최근 활동 확인용
     * 
     * @param userId 사용자 ID
     * @param limit 제한 개수
     * @return 최신 포스트 목록
     */
    public List<Post> findLatestUserPosts(String userId, int limit) {
        log.debug("Finding latest user posts for external aggregate: userId={}, limit={}", userId, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 특정 기간 동안의 인기 포스트 조회
     * 추천 서비스에서 트렌딩 포스트 분석용
     * 
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @param limit 제한 개수
     * @return 인기 포스트 목록
     */
    public List<Post> findTrendingPostsInPeriod(Instant startTime, Instant endTime, int limit) {
        log.debug("Finding trending posts in period for external aggregate: start={}, end={}, limit={}", 
                startTime, endTime, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByIsActiveTrueAndCreatedAtBetweenOrderByLikesCountDescCreatedAtDesc(
                startTime, endTime, pageable);
    }

    /**
     * 포스트 존재 여부 확인 (외부 aggregate용)
     * Like 서비스, Comment 서비스에서 포스트 존재 확인용
     * 
     * @param postId 포스트 ID
     * @return 존재 여부
     */
    public boolean existsActivePost(String postId) {
        log.debug("Checking active post existence for external aggregate: postId={}", postId);
        return postRepository.existsByPostIdAndIsActiveTrue(postId);
    }

    /**
     * 사용자가 작성한 포스트 ID 목록 조회
     * Follow 서비스에서 팔로우 대상의 포스트 목록 확인용
     * 
     * @param userId 사용자 ID
     * @param limit 제한 개수
     * @return 포스트 ID 목록
     */
    public List<String> findUserPostIds(String userId, int limit) {
        log.debug("Finding user post ids for external aggregate: userId={}, limit={}", userId, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findTopByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId, pageable)
                .stream()
                .map(Post::getPostId)
                .toList();
    }

    /**
     * 특정 태그들을 포함한 포스트 조회
     * 추천 서비스에서 사용자 관심사 기반 포스트 추천용
     * 
     * @param tags 태그 목록
     * @param limit 제한 개수
     * @return 관련 포스트 목록
     */
    public List<Post> findPostsByTags(List<String> tags, int limit) {
        log.debug("Finding posts by tags for external aggregate: tags={}, limit={}", tags, limit);
        Pageable pageable = PageRequest.of(0, limit);
        return postRepository.findByTagsInAndIsActiveTrueOrderByCreatedAtDesc(tags, pageable);
    }

    // === PostController용 메서드들 (캐싱 적용) ===

    /**
     * 포스트 ID로 단일 포스트를 조회합니다. (캐싱 적용)
     */
    @Cacheable(value = "posts", key = "#postId")
    public Optional<Post> findById(String postId) {
        log.debug("Finding post by id: {}", postId);
        return postRepository.findByPostId(postId)
                .filter(Post::getIsActive);
    }

    /**
     * 사용자의 포스트를 페이징하여 조회합니다. (캐싱 적용)
     */
    @Cacheable(value = "userFeed", key = "@postCacheKeyGenerator.generateUserFeedKey(#userId, #scrollRequest)")
    public ScrollResponse<Post> findUserPostsWithPaging(String userId, ScrollRequest scrollRequest) {
        log.debug("Finding user posts with paging: userId={}, cursor={}", userId, scrollRequest.getCursor());
        
        return paginationService.paginateWithQuery(
                request -> findUserPosts(userId, request),
                scrollRequest,
                Post::getPostId
        );
    }

    /**
     * 인기 포스트(트렌딩)를 페이징하여 조회합니다. (캐싱 적용)
     */
    @Cacheable(value = "trendingPosts", key = "@postCacheKeyGenerator.generateTrendingKey(#scrollRequest)")
    public ScrollResponse<Post> findTrendingPostsWithPaging(ScrollRequest scrollRequest) {
        log.debug("Finding trending posts with paging: cursor={}", scrollRequest.getCursor());
        
        List<Post> allPosts = findTrendingPosts();
        return paginationService.paginateWithSort(
                allPosts,
                scrollRequest,
                Post::getPostId,
                post -> post.getLikesCount().toString()
        );
    }

    /**
     * 태그별 포스트를 페이징하여 조회합니다. (캐싱 적용)
     */
    @Cacheable(value = "tagPosts", key = "@postCacheKeyGenerator.generateTagKey(#tag, #scrollRequest)")
    public ScrollResponse<Post> findPostsByTagWithPaging(String tag, ScrollRequest scrollRequest) {
        log.debug("Finding posts by tag with paging: tag={}, cursor={}", tag, scrollRequest.getCursor());
        
        List<Post> allPosts = findPostsByTag(tag);
        return paginationService.paginate(allPosts, scrollRequest, Post::getPostId);
    }

    /**
     * 내용으로 포스트를 검색합니다. (캐싱 적용)
     */
    @Cacheable(value = "searchPosts", key = "@postCacheKeyGenerator.generateSearchKey(#content, #scrollRequest)")
    public ScrollResponse<Post> searchPostsWithPaging(String content, ScrollRequest scrollRequest) {
        log.debug("Searching posts with paging: content={}, cursor={}", content, scrollRequest.getCursor());
        
        List<Post> results = searchPostsByContent(content);
        return paginationService.paginate(results, scrollRequest, Post::getPostId);
    }

    /**
     * 포스트 존재 여부를 확인합니다.
     */
    public boolean existsById(String postId) {
        log.debug("Checking post existence: postId={}", postId);
        return postRepository.existsByPostIdAndIsActiveTrue(postId);
    }

    /**
     * 최근 포스트 수를 조회합니다.
     */
    public long countRecentPostsByHours(String userId, int sinceHours) {
        log.debug("Counting recent posts: userId={}, sinceHours={}", userId, sinceHours);
        
        Instant since = Instant.now().minus(sinceHours, java.time.temporal.ChronoUnit.HOURS);
        return countRecentPosts(userId, since);
    }
}