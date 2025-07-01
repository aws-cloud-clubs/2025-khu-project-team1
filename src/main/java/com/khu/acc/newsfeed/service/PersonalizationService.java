package com.khu.acc.newsfeed.service;

import com.khu.acc.newsfeed.model.Post;
import com.khu.acc.newsfeed.model.User;
import com.khu.acc.newsfeed.repository.LikeRepository;
import com.khu.acc.newsfeed.repository.PostRepository;
import com.khu.acc.newsfeed.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalizationService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    // 개인화 가중치 설정
    private static final double INTEREST_WEIGHT = 0.4;      // 관심사 가중치
    private static final double ENGAGEMENT_WEIGHT = 0.3;    // 참여도 가중치
    private static final double RECENCY_WEIGHT = 0.2;       // 최신성 가중치
    private static final double AUTHOR_WEIGHT = 0.1;        // 작성자 관계 가중치

    /**
     * 포스트들에 대한 개인화 점수 계산 및 정렬
     */
    public List<Post> calculatePersonalizationScores(String userId, List<Post> posts) {
        log.debug("Calculating personalization scores for {} posts for user: {}", posts.size(), userId);

        // 사용자 개인화 데이터 조회
        UserPersonalizationData userData = getUserPersonalizationData(userId);

        // 각 포스트에 대한 점수 계산
        Map<String, Double> postScores = new HashMap<>();

        for (Post post : posts) {
            double score = calculatePostScore(post, userData);
            postScores.put(post.getPostId(), score);
        }

        // 점수 기준으로 정렬
        return posts.stream()
                .sorted((p1, p2) -> Double.compare(
                        postScores.getOrDefault(p2.getPostId(), 0.0),
                        postScores.getOrDefault(p1.getPostId(), 0.0)))
                .collect(Collectors.toList());
    }

    /**
     * 개별 포스트의 개인화 점수 계산
     */
    private double calculatePostScore(Post post, UserPersonalizationData userData) {
        double interestScore = calculateInterestScore(post, userData.interests);
        double engagementScore = calculateEngagementScore(post);
        double recencyScore = calculateRecencyScore(post);
        double authorScore = calculateAuthorScore(post, userData);

        double totalScore = (interestScore * INTEREST_WEIGHT) +
                (engagementScore * ENGAGEMENT_WEIGHT) +
                (recencyScore * RECENCY_WEIGHT) +
                (authorScore * AUTHOR_WEIGHT);

        log.debug("Post {} scores - Interest: {}, Engagement: {}, Recency: {}, Author: {}, Total: {}",
                post.getPostId(), interestScore, engagementScore, recencyScore, authorScore, totalScore);

        return totalScore;
    }

    /**
     * 관심사 기반 점수 계산
     */
    private double calculateInterestScore(Post post, Set<String> userInterests) {
        if (userInterests.isEmpty() || post.getTags() == null || post.getTags().isEmpty()) {
            return 0.5; // 기본 점수
        }

        // 사용자 관심사와 포스트 태그의 교집합 계산
        Set<String> intersection = new HashSet<>(userInterests);
        intersection.retainAll(post.getTags());

        if (intersection.isEmpty()) {
            return 0.3;
        }

        // 매칭 비율에 따른 점수 (0.5 ~ 1.0)
        double matchRatio = (double) intersection.size() / Math.max(userInterests.size(), post.getTags().size());
        return 0.5 + (matchRatio * 0.5);
    }

    /**
     * 참여도 기반 점수 계산 (좋아요, 댓글 수)
     */
    private double calculateEngagementScore(Post post) {
        long likesCount = post.getLikesCount() != null ? post.getLikesCount() : 0;
        long commentsCount = post.getCommentsCount() != null ? post.getCommentsCount() : 0;

        // 참여도를 0~1 범위로 정규화 (로그 스케일 사용)
        double engagementValue = Math.log(1 + likesCount + (commentsCount * 2)) / 10.0;
        return Math.min(1.0, engagementValue);
    }

    /**
     * 최신성 점수 계산
     */
    private double calculateRecencyScore(Post post) {
        Instant now = Instant.now();
        Instant postTime = post.getCreatedAt();

        // 24시간 이내: 1.0, 일주일 이후: 0.1
        long hoursAgo = ChronoUnit.HOURS.between(postTime, now);

        if (hoursAgo <= 24) {
            return 1.0;
        } else if (hoursAgo <= 168) { // 1주일
            return 1.0 - (hoursAgo - 24) / 168.0 * 0.9;
        } else {
            return 0.1;
        }
    }

    /**
     * 작성자 관계 점수 계산
     */
    private double calculateAuthorScore(Post post, UserPersonalizationData userData) {
        String authorId = post.getUserId();

        // 자주 상호작용하는 사용자인지 확인
        if (userData.frequentlyInteractedUsers.contains(authorId)) {
            return 1.0;
        }

        return 0.5; // 기본 점수
    }

    /**
     * 사용자 개인화 데이터 조회 (캐시 적용)
     */
    @Cacheable(value = "userPersonalizationData", key = "#userId")
    public UserPersonalizationData getUserPersonalizationData(String userId) {
        log.debug("Loading personalization data for user: {}", userId);

        // 사용자 관심사 조회
        Set<String> interests = getUserInterests(userId);

        // 최근 상호작용한 사용자들 조회
        Set<String> frequentUsers = getFrequentlyInteractedUsers(userId);

        // 최근 좋아요한 태그들 조회
        Set<String> preferredTags = getRecentlyLikedTags(userId);

        return new UserPersonalizationData(interests, frequentUsers, preferredTags);
    }

    /**
     * 사용자 관심사 태그 기반 포스트 조회
     */
    public List<Post> getPostsByUserInterests(User user, Pageable pageable) {
        if (user.getInterests() == null || user.getInterests().isEmpty()) {
            return Collections.emptyList();
        }

        // 관심사 태그가 포함된 포스트들 조회
        List<Post> interestPosts = new ArrayList<>();
        for (String interest : user.getInterests()) {
            List<Post> tagPosts = postRepository.findByTagsContainingAndIsActiveTrueOrderByCreatedAtDesc(
                    interest, PageRequest.of(0, 10));
            interestPosts.addAll(tagPosts);
        }

        // 중복 제거 및 개인화 점수로 정렬
        Map<String, Post> uniquePosts = interestPosts.stream()
                .collect(Collectors.toMap(Post::getPostId, p -> p, (p1, p2) -> p1));

        List<Post> filteredPosts = new ArrayList<>(uniquePosts.values());

        // 개인화 점수 계산 및 정렬
        return calculatePersonalizationScores(user.getUserId(), filteredPosts)
                .stream()
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }

    /**
     * 사용자 관심사 조회
     */
    private Set<String> getUserInterests(String userId) {
        // TODO: UserRepository에서 사용자 정보 조회하여 관심사 반환
        return new HashSet<>();
    }

    /**
     * 최근 자주 상호작용한 사용자들 조회
     */
    private Set<String> getFrequentlyInteractedUsers(String userId) {
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);

        // 최근 30일간 좋아요한 포스트의 작성자들
        Set<String> likedAuthors = likeRepository.findByUserIdAndCreatedAtAfter(userId, since)
                .stream()
                .map(like -> {
                    // TODO: Post 조회하여 작성자 ID 반환
                    return ""; // 임시
                })
                .collect(Collectors.toSet());

        // 최근 30일간 댓글 단 포스트의 작성자들
        Set<String> commentedAuthors = commentRepository.findByUserIdAndCreatedAtAfter(userId, since)
                .stream()
                .map(comment -> {
                    // TODO: Post 조회하여 작성자 ID 반환
                    return ""; // 임시
                })
                .collect(Collectors.toSet());

        Set<String> result = new HashSet<>(likedAuthors);
        result.addAll(commentedAuthors);

        return result;
    }

    /**
     * 최근 좋아요한 포스트의 태그들 조회
     */
    private Set<String> getRecentlyLikedTags(String userId) {
        Instant since = Instant.now().minus(14, ChronoUnit.DAYS);

        return likeRepository.findByUserIdAndCreatedAtAfter(userId, since)
                .stream()
                .map(like -> {
                    // TODO: Post 조회하여 태그 반환
                    return new HashSet<String>(); // 임시
                })
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

}