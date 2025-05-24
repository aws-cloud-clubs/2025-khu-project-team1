package com.khu.acc.newsfeed.dto;

import com.khu.acc.newsfeed.model.Like;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {

    private String postId;
    private String userId;
    private Instant createdAt;

    // 추가 필드
    private UserResponse user;

    public static LikeResponse from(Like like) {
        return LikeResponse.builder()
                .postId(like.getPostId())
                .userId(like.getUserId())
                .createdAt(like.getCreatedAt())
                .build();
    }
}
