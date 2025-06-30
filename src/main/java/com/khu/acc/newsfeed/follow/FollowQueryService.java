package com.khu.acc.newsfeed.follow;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Follow 애그리게이트 전용 쿼리 서비스
 * 다른 도메인에서 Follow 정보가 필요할 때 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FollowQueryService {
    
    private final FollowRepository followRepository;
    
    /**
     * 팬아웃을 위한 팔로워 ID 목록 조회
     * 캐시 TTL: 5분 (자주 변경될 수 있으므로 짧게 설정)
     * 
     * @param userId 조회할 사용자 ID
     * @return 팔로워 ID 목록
     */
    @Cacheable(value = "followers", key = "#userId")
    public List<String> getFollowerIds(String userId) {
        log.debug("Fetching follower IDs for user: {}", userId);
        
        List<String> followerIds = followRepository.findByFolloweeId(userId).stream()
                .map(Follow::getFollowerId)
                .collect(Collectors.toList());
        
        log.debug("Found {} followers for user: {}", followerIds.size(), userId);
        return followerIds;
    }
    
    /**
     * 뉴스피드 조회를 위한 팔로잉 ID 목록
     * 캐시 TTL: 5분 (자주 변경될 수 있으므로 짧게 설정)
     * 
     * @param userId 조회할 사용자 ID
     * @return 팔로잉 ID 목록
     */
    @Cacheable(value = "followings", key = "#userId")
    public List<String> getFollowingIds(String userId) {
        log.debug("Fetching following IDs for user: {}", userId);
        
        List<String> followingIds = followRepository.findByFollowerId(userId).stream()
                .map(Follow::getFolloweeId)
                .collect(Collectors.toList());
        
        log.debug("Found {} followings for user: {}", followingIds.size(), userId);
        return followingIds;
    }
    
    /**
     * 팔로우 관계 확인
     * 
     * @param followerId 팔로워 ID
     * @param followeeId 팔로이 ID
     * @return 팔로우 여부
     */
    @Cacheable(value = "followRelation", key = "#followerId + ':' + #followeeId")
    public boolean isFollowing(String followerId, String followeeId) {
        return followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
    }
    
    /**
     * 팔로우 이벤트 시 캐시 즉시 무효화
     * 
     * @param followeeId 팔로이 ID
     */
    @CacheEvict(value = {"followers"}, key = "#followeeId")
    public void evictFollowerCache(String followeeId) {
        log.debug("Evicted follower cache for user: {}", followeeId);
    }
    
    /**
     * 팔로잉 이벤트 시 캐시 즉시 무효화
     * 
     * @param followerId 팔로워 ID
     */
    @CacheEvict(value = {"followings"}, key = "#followerId")
    public void evictFollowingCache(String followerId) {
        log.debug("Evicted following cache for user: {}", followerId);
    }
    
    /**
     * 팔로우 관계 캐시 무효화
     * 
     * @param followerId 팔로워 ID
     * @param followeeId 팔로이 ID
     */
    @CacheEvict(value = "followRelation", key = "#followerId + ':' + #followeeId")
    public void evictFollowRelationCache(String followerId, String followeeId) {
        log.debug("Evicted follow relation cache for: {} -> {}", followerId, followeeId);
    }
    
    /**
     * 특정 사용자의 모든 팔로우 관련 캐시 무효화
     * 
     * @param userId 사용자 ID
     */
    @CacheEvict(value = {"followers", "followings"}, key = "#userId")
    public void evictAllFollowCacheForUser(String userId) {
        log.debug("Evicted all follow caches for user: {}", userId);
    }
}