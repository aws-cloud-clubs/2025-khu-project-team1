package com.khu.acc.newsfeed.post.post.interfaces.dto;

import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
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
    
    /**
     * 작성자 정보와 함께 PostResponse 생성 (필요시 확장용)
     */
    public static PostResponse withAuthor(Post post, UserResponse author) {
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
                .author(author)
                .build();
    }
}