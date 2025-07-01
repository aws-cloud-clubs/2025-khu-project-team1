package com.khu.acc.newsfeed.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 사용자 개인화 데이터 DTO
 */
public class UserPersonalizationData {
    public final Set<String> interests;
    public final Set<String> frequentlyInteractedUsers;
    public final Set<String> recentlyLikedTags;

    public UserPersonalizationData(Set<String> interests,
                                   Set<String> frequentlyInteractedUsers,
                                   Set<String> recentlyLikedTags) {
        this.interests = interests != null ? interests : new HashSet<>();
        this.frequentlyInteractedUsers = frequentlyInteractedUsers != null ? frequentlyInteractedUsers : new HashSet<>();
        this.recentlyLikedTags = recentlyLikedTags != null ? recentlyLikedTags : new HashSet<>();
    }
}