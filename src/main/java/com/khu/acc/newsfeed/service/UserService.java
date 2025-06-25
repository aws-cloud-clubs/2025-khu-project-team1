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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
        return userRepository.findById(userId);
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

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> searchUsersByUsername(String username) {
        return userRepository.findAllByUsername(username);
    }

    public Page<User> findActiveUsers(Pageable pageable) {
        return userRepository.findByIsActive("true", pageable);
    }

    public void deactivateUser(String userId) {
    }
}