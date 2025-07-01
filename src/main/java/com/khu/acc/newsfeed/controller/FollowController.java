package com.khu.acc.newsfeed.service;

import com.khu.acc.newsfeed.dto.FollowResponse;
import com.khu.acc.newsfeed.model.Follow;
import com.khu.acc.newsfeed.model.User;
import com.khu.acc.newsfeed.repository.FollowRepository;
import com.khu.acc.newsfeed.repository.UserRepository;
import com.khu.acc.newsfeed.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NewsFeedService newsFeedService;

    /**
     * 사용자 팔로우
     */
    public FollowResponse followUser(String followerId, String followeeId) {
        // 자기 자신을 팔로우할 수 없음
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId)) {
            throw new IllegalStateException("Already following this user");
        }

        // 팔로우할 사용자가 존재하는지 확인
        User followee = userRepository.findById(followeeId)
                .orElseThrow(() -> ResourceNotFoundException.user(followeeId));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> ResourceNotFoundException.user(followerId));

        // 팔로우 관계 생성
        Follow follow = Follow.builder()
                .followId(generateFollowId())
                .followerId(followerId)
                .followeeId(followeeId)
                .createdAt(Instant.now())
                .build();

        followRepository.save(follow);

        // 사용자 통계 업데이트
        updateUserFollowCounts(followerId, followeeId, true);

        // 캐시 무효화
        evictFollowCaches(followerId, followeeId);

        log.info("User {} started following user {}", followerId, followeeId);

        return FollowResponse.from(follow);
    }

    /**
     * 사용자 언팔로우
     */
    public void unfollowUser(String followerId, String followeeId) {
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new IllegalStateException("Not following this user"));

        followRepository.delete(follow);

        // 사용자 통계 업데이트
        updateUserFollowCounts(followerId, followeeId, false);

        // 캐시 무효화
        evictFollowCaches(followerId, followeeId);

        log.info("User {} stopped following user {}", followerId, followeeId);
    }

    /**
     * 팔로잉 목록 조회
     */
    @Cacheable(value = "followings", key = "#userId + '_' + #pageable.pageNumber")
    public Page<FollowResponse> getFollowing(String userId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        return follows.map(follow -> {
            FollowResponse response = FollowResponse.from(follow);
            // 팔로우된 사용자 정보 추가
            userRepository.findById(follow.getFolloweeId())
                    .ifPresent(user -> response.setFollowee(
                            com.khu.acc.newsfeed.dto.UserResponse.from(user)));
            return response;
        });
    }

    /**
     * 팔로워 목록 조회
     */
    @Cacheable(value = "followers", key = "#userId + '_' + #pageable.pageNumber")
    public Page<FollowResponse> getFollowers(String userId, Pageable pageable) {
        Page<Follow> follows = followRepository.findByFolloweeIdOrderByCreatedAtDesc(userId, pageable);

        return follows.map(follow -> {
            FollowResponse response = FollowResponse.from(follow);
            // 팔로워 사용자 정보 추가
            userRepository.findById(follow.getFollowerId())
                    .ifPresent(user -> response.setFollower(
                            com.khu.acc.newsfeed.dto.UserResponse.from(user)));
            return response;
        });
    }

    /**
     * 팔로우 상태 확인
     */
    @Cacheable(value = "followStatus", key = "#followerId + '_' + #followeeId")
    public boolean isFollowing(String followerId, String followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }

    /**
     * 팔로우 통계 조회
     */
    @Cacheable(value = "followStats", key = "#userId")
    public FollowStats getFollowStats(String userId) {
        Long followersCount = followRepository.countByFolloweeId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        return new FollowStats(followersCount, followingCount);
    }

    /**
     * 팔로잉하는 사용자 ID 목록 조회 (개인화 피드용)
     */
    @Cacheable(value = "followingUserIds", key = "#userId")
    public List<String> getFollowingUserIds(String userId) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());
    }

    /**
     * 팔로워 사용자 ID 목록 조회
     */
    @Cacheable(value = "followerUserIds", key = "#userId")
    public List<String> getFollowerUserIds(String userId) {
        return followRepository.findByFolloweeIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
    }

    /**
     * 상호 팔로우 여부 확인
     */
    public boolean isMutualFollow(String userId1, String userId2) {
        return isFollowing(userId1, userId2) && isFollowing(userId2, userId1);
    }

    /**
     * 추천 팔로우 사용자 목록 (간단한 로직)
     */
    @Cacheable(value = "recommendedUsers", key = "#userId")
    public List<String> getRecommendedUsers(String userId, int limit) {
        // 팔로잉하는 사용자들의 팔로잉 목록에서 추천
        List<String> followingIds = getFollowingUserIds(userId);
        List<String> currentFollowingIds = getFollowingUserIds(userId);

        return followingIds.stream()
                .flatMap(followingId -> getFollowingUserIds(followingId).stream())
                .filter(recommendedId -> !recommendedId.equals(userId)) // 자기 자신 제외
                .filter(recommendedId -> !currentFollowingIds.contains(recommendedId)) // 이미 팔로우 중인 사용자 제외
                .distinct()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 사용자 팔로우 수 업데이트
     */
    private void updateUserFollowCounts(String followerId, String followeeId, boolean isFollow) {
        // 팔로워 수 업데이트 (팔로우 받는 사용자)
        userRepository.findById(followeeId).ifPresent(followee -> {
            if (isFollow) {
                followee.incrementFollowersCount();
            } else {
                followee.decrementFollowersCount();
            }
            userRepository.save(followee);
        });

        // 팔로잉 수 업데이트 (팔로우 하는 사용자)
        userRepository.findById(followerId).ifPresent(follower -> {
            if (isFollow) {
                follower.incrementFollowingCount();
            } else {
                follower.decrementFollowingCount();
            }
            userRepository.save(follower);
        });
    }

    /**
     * 팔로우 관련 캐시 무효화
     */
    @CacheEvict(value = {"followings", "followers", "followStatus", "followStats", "followingUserIds", "followerUserIds", "recommendedUsers"},
            key = "#followerId")
    private void evictFollowerCaches(String followerId) {
        // 팔로워의 캐시 무효화
    }

    @CacheEvict(value = {"followings", "followers", "followStatus", "followStats", "followingUserIds", "followerUserIds", "recommendedUsers"},
            key = "#followeeId")
    private void evictFolloweeCaches(String followeeId) {
        // 팔로우 받는 사용자의 캐시 무효화
    }

    private void evictFollowCaches(String followerId, String followeeId) {
        evictFollowerCaches(followerId);
        evictFolloweeCaches(followeeId);

        // 뉴스 피드 캐시도 무효화
        if (newsFeedService != null) {
            newsFeedService.invalidateUserFeedCache(followerId);
        }
    }

    /**
     * Follow ID 생성
     */
    private String generateFollowId() {
        return "follow_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 팔로우 통계 DTO
     */
    public record FollowStats(Long followersCount, Long followingCount) {}
}