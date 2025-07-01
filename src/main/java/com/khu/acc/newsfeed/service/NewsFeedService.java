package com.khu.acc.newsfeed.service;

import com.khu.acc.newsfeed.dto.PostResponse;
import com.khu.acc.newsfeed.model.Post;
import com.khu.acc.newsfeed.model.User;
import com.khu.acc.newsfeed.repository.PostRepository;
import com.khu.acc.newsfeed.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class NewsFeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FollowService followService;
    private final PersonalizationService personalizationService;

    /**
     * 사용자의 개인화된 뉴스 피드 조회 (캐시 적용)
     */
    @Cacheable(value = "newsFeed", key = "#userId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public List<PostResponse> getPersonalizedNewsFeed(String userId, Pageable pageable) {
        log.info("Generating personalized news feed for user: {}", userId);

        try {
            // 1. 팔로잉 기반 피드 가져오기
            List<Post> followingPosts = getFollowingPosts(userId, pageable);

            // 2. 개인화 점수 계산 및 정렬
            List<PostResponse> personalizedPosts = personalizationService
                    .calculatePersonalizationScores(userId, followingPosts)
                    .stream()
                    .map(this::convertToPostResponse)
                    .collect(Collectors.toList());

            log.info("Generated {} personalized posts for user: {}", personalizedPosts.size(), userId);
            return personalizedPosts;

        } catch (Exception e) {
            log.error("Error generating personalized feed for user: {}", userId, e);
            // 오류 시 기본 피드 반환
            return getDefaultFeed(pageable);
        }
    }

    /**
     * 관심사 기반 개인화 피드
     */
    @Cacheable(value = "userFeed", key = "#userId + '_interests_' + #pageable.pageNumber")
    public List<PostResponse> getInterestBasedFeed(String userId, Pageable pageable) {
        log.info("Generating interest-based feed for user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return getDefaultFeed(pageable);
        }

        User user = userOpt.get();
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            // 관심사가 없는 경우 팔로잉 기반 피드 반환
            return getPersonalizedNewsFeed(userId, pageable);
        }

        // 관심사 태그가 포함된 포스트 검색
        List<Post> interestPosts = personalizationService.getPostsByUserInterests(user, pageable);

        return interestPosts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    /**
     * 팔로잉한 사용자들의 포스트 조회
     */
    private List<Post> getFollowingPosts(String userId, Pageable pageable) {
        // 팔로잉 사용자 ID 목록 조회
        List<String> followingUserIds = followService.getFollowingUserIds(userId);

        if (followingUserIds.isEmpty()) {
            log.info("User {} has no following. Returning trending posts", userId);
            return getTrendingPosts(pageable);
        }

        // 팔로잉 사용자들의 최근 포스트 조회
        Instant since = Instant.now().minus(7, ChronoUnit.DAYS); // 최근 7일
        return postRepository.findByUserIdInAndCreatedAtAfterAndIsActiveTrueOrderByCreatedAtDesc(
                followingUserIds, since, pageable);
    }

    /**
     * 트렌딩 포스트 조회 (팔로잉이 없는 경우 대체)
     */
    private List<Post> getTrendingPosts(Pageable pageable) {
        Instant since = Instant.now().minus(1, ChronoUnit.DAYS); // 최근 24시간
        return postRepository.findByCreatedAtAfterAndIsActiveTrueOrderByLikesCountDescCommentsCountDesc(
                since, pageable);
    }

    /**
     * 기본 피드 (오류 시 대체)
     */
    private List<PostResponse> getDefaultFeed(Pageable pageable) {
        log.info("Returning default feed");
        List<Post> posts = postRepository.findByIsActiveTrueOrderByCreatedAtDesc(pageable);
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    /**
     * Post를 PostResponse로 변환
     */
    private PostResponse convertToPostResponse(Post post) {
        PostResponse response = PostResponse.from(post);

        // 작성자 정보 추가
        userRepository.findById(post.getUserId())
                .ifPresent(user -> response.setAuthor(
                        com.khu.acc.newsfeed.dto.UserResponse.from(user)));

        return response;
    }

    /**
     * 사용자 피드 캐시 무효화
     */
    @CacheEvict(value = {"newsFeed", "userFeed"}, key = "#userId + '*'")
    public void invalidateUserFeedCache(String userId) {
        log.info("Invalidated feed cache for user: {}", userId);
    }

    /**
     * 전체 피드 캐시 새로고침
     */
    @CacheEvict(value = {"newsFeed", "userFeed"}, allEntries = true)
    public void refreshAllFeedCache() {
        log.info("Refreshed all feed cache");
    }

    /**
     * 사용자의 피드 통계 조회
     */
    @Cacheable(value = "userFeedStats", key = "#userId")
    public FeedStats getUserFeedStats(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return new FeedStats(0L, 0L, 0L);
        }

        User user = userOpt.get();
        List<String> followingIds = followService.getFollowingUserIds(userId);

        // 팔로잉 사용자들의 오늘 포스트 수
        Instant today = Instant.now().truncatedTo(ChronoUnit.DAYS);
        Long todayPostsCount = postRepository.countByUserIdInAndCreatedAtAfterAndIsActiveTrue(
                followingIds, today);

        return new FeedStats(
                user.getFollowingCount(),
                todayPostsCount,
                (long) followingIds.size()
        );
    }

    /**
     * 피드 통계 DTO
     */
    public record FeedStats(
            Long followingCount,
            Long todayPostsCount,
            Long activeFollowingCount
    ) {}
}