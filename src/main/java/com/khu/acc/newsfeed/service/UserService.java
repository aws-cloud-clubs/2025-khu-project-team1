package com.khu.acc.newsfeed.service;

import com.khu.acc.newsfeed.model.User;
import com.khu.acc.newsfeed.repository.UserRepository;
import jakarta.validation.constraints.Size;
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
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * 새로운 사용자 생성
     */
    public User createUser(String email, String username, String displayName) {
        log.info("Creating new user with email: {}, username: {}", email, username);

        // 입력 유효성 검사
        validateUserCreation(email, username);

        // 새 사용자 생성
        User newUser = User.builder()
                .userId(generateUserId())
                .email(email)
                .username(username)
                .displayName(displayName)
                .followersCount(0L)
                .followingCount(0L)
                .postsCount(0L)
                .isActive("true")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Successfully created user with ID: {}", savedUser.getUserId());

        return savedUser;
    }

    /**
     * 사용자 ID로 조회 (캐시 적용)
     */
    @Cacheable(value = "users", key = "#userId")
    public Optional<User> findById(String userId) {
        return userRepository.findById(userId);
    }

    /**
     * 사용자명으로 조회 (캐시 적용)
     */
    @Cacheable(value = "users", key = "'username_' + #username")
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 이메일로 조회 (캐시 적용)
     */
    @Cacheable(value = "users", key = "'email_' + #email")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 프로필 업데이트
     */
    @CacheEvict(value = "users", key = "#user.userId")
    public User updateUserProfile(User user,
                                  @Size(max = 100, message = "Display name must not exceed 100 characters") String displayName,
                                  @Size(max = 500, message = "Bio must not exceed 500 characters") String bio,
                                  String profileImageUrl,
                                  Set<String> interests) {

        log.info("Updating profile for user: {}", user.getUserId());

        // 필드 업데이트
        if (displayName != null && !displayName.trim().isEmpty()) {
            user.setDisplayName(displayName.trim());
        }

        if (bio != null) {
            user.setBio(bio.trim());
        }

        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            user.setProfileImageUrl(profileImageUrl.trim());
        }

        if (interests != null) {
            user.setInterests(interests);
        }

        user.setUpdatedAt(Instant.now());

        User updatedUser = userRepository.save(user);
        log.info("Successfully updated profile for user: {}", user.getUserId());

        return updatedUser;
    }

    /**
     * 사용자명으로 검색
     */
    public List<User> searchUsersByUsername(String username) {
        log.debug("Searching users by username: {}", username);
        return userRepository.findAllByUsername(username);
    }

    /**
     * 활성 사용자 목록 조회
     */
    public Page<User> findActiveUsers(Pageable pageable) {
        return userRepository.findByIsActive("true", pageable);
    }

    /**
     * 사용자 비활성화
     */
    @CacheEvict(value = "users", key = "#userId")
    public void deactivateUser(String userId) {
        log.info("Deactivating user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive("false");
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            log.info("Successfully deactivated user: {}", userId);
        } else {
            log.warn("User not found for deactivation: {}", userId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    /**
     * 사용자 활성화
     */
    @CacheEvict(value = "users", key = "#userId")
    public void activateUser(String userId) {
        log.info("Activating user: {}", userId);

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setIsActive("true");
            user.setUpdatedAt(Instant.now());
            userRepository.save(user);

            log.info("Successfully activated user: {}", userId);
        } else {
            log.warn("User not found for activation: {}", userId);
            throw new IllegalArgumentException("User not found: " + userId);
        }
    }

    /**
     * 사용자 통계 조회
     */
    @Cacheable(value = "userStats", key = "#userId")
    public UserStats getUserStats(String userId) {
        Optional<User> userOpt = findById(userId);
        if (userOpt.isEmpty()) {
            return new UserStats(0L, 0L, 0L);
        }

        User user = userOpt.get();
        return new UserStats(
                user.getFollowersCount() != null ? user.getFollowersCount() : 0L,
                user.getFollowingCount() != null ? user.getFollowingCount() : 0L,
                user.getPostsCount() != null ? user.getPostsCount() : 0L
        );
    }

    /**
     * 전체 활성 사용자 수 조회
     */
    @Cacheable(value = "activeUserCount")
    public Long getActiveUserCount() {
        return userRepository.countByIsActiveTrue();
    }

    /**
     * 사용자 생성 시 유효성 검사
     */
    private void validateUserCreation(String email, String username) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    /**
     * 사용자 ID 생성
     */
    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 사용자 캐시 무효화
     */
    @CacheEvict(value = {"users", "userStats"}, key = "#userId")
    public void evictUserCache(String userId) {
        log.debug("Evicted cache for user: {}", userId);
    }

    /**
     * 전체 사용자 캐시 무효화
     */
    @CacheEvict(value = {"users", "userStats", "activeUserCount"}, allEntries = true)
    public void evictAllUserCache() {
        log.info("Evicted all user cache");
    }

    /**
     * 사용자 통계 DTO
     */
    public record UserStats(
            Long followersCount,
            Long followingCount,
            Long postsCount
    ) {}
}