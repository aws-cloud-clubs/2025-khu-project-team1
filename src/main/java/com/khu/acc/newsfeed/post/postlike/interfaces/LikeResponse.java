package com.khu.acc.newsfeed.post.postlike.interfaces;

import com.khu.acc.newsfeed.post.postlike.domain.Like;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponse {

    private String postId;
    private String userId;
    private Instant createdAt;

    // 추가 필드
    private UserResponse user;

    // Static factory methods
    public static LikeResponse from(Like like) {
        LikeResponse response = new LikeResponse();
        response.postId = like.getPostId();
        response.userId = like.getUserId();
        response.createdAt = like.getCreatedAt();
        return response;
    }

    public static LikeResponse withUser(Like like, UserResponse user) {
        LikeResponse response = from(like);
        response.user = user;
        return response;
    }
}
