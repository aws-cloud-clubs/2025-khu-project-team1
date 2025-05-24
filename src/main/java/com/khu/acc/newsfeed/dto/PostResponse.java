package com.khu.acc.newsfeed.dto;

import com.khu.acc.newsfeed.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private String postId;
    private String userId;
    private String content;
    private List<String> imageUrls;
    private Long likesCount;
    private Long commentsCount;
    private Set<String> tags;
    private String location;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;

    // 추가 필드 (필요시)
    private UserResponse author;
    private Boolean isLikedByCurrentUser;

    public static PostResponse from(Post post) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .userId(post.getUserId())
                .content(post.getContent())
                .imageUrls(post.getImageUrls())
                .likesCount(post.getLikesCount())
                .commentsCount(post.getCommentsCount())
                .tags(post.getTags())
                .location(post.getLocation())
                .isActive(post.getIsActive())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}