package com.khu.acc.newsfeed.user.application;


import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.user.domain.UserRepository;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String username, String displayName) {
        return null;
    }

    @Cacheable(value = "users", key = "#userId")
    public Optional<User> findById(String userId) {
        return userRepository.findByUserId(userId);
    }

    private void validateUserCreation(String email, String username) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists: " + email);
        }
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }

    private String generateUserId() {
        return "user_" + UUID.randomUUID().toString().replace("-", "");
    }

    public User updateUserProfile(User user, @Size(max = 100, message = "Display name must not exceed 100 characters") String displayName, @Size(max = 500, message = "Bio must not exceed 500 characters") String bio, String profileImageUrl, Set<String> interests) {
        return user;
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findAllByUsername(username);
    }

    public Page<User> findActiveUsers(Pageable pageable) {
        return userRepository.findByIsActive("true", pageable);
    }

    public void deactivateUser(String userId) {
    }

    /**
     * 배치로 사용자들을 조회
     */
    @Cacheable(value = "usersBatch", key = "#userIds.hashCode()")
    public Map<String, User> findUsersByIds(Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // UserRepository의 배치 조회 메서드를 호출합니다.
        List<User> users = userRepository.findAllByUserIds(userIds);

        // 조회된 사용자 리스트를 userId를 키로 하는 맵으로 변환합니다.
        return users.stream()
                .collect(Collectors.toMap(User::getUserId, Function.identity()));
    }

    /**
     * 사용자 ID 리스트로 사용자 조회 (순서 보장)
     */
    public List<User> findUsersByIdList(List<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Map<String, User> userMap = findUsersByIds(new HashSet<>(userIds));
        return userIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}