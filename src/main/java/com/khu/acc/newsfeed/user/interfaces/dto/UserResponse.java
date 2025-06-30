package com.khu.acc.newsfeed.user.interfaces.dto;

import com.khu.acc.newsfeed.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String userId;
    private String email;
    private String username;
    private String displayName;
    private String profileImageUrl;
    private String bio;
    private Long followersCount;
    private Long followingCount;
    private Long postsCount;
    private Set<String> interests;
    private String isActive;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .profileImageUrl(user.getProfileImageUrl())
                .bio(user.getBio())
                .followersCount(user.getFollowersCount())
                .followingCount(user.getFollowingCount())
                .postsCount(user.getPostsCount())
                .interests(user.getInterests())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}