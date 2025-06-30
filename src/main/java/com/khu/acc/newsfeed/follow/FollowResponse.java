package com.khu.acc.newsfeed.follow;

import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowResponse {

    private String followerId;
    private String followeeId;
    private Instant createdAt;

    // 추가 필드
    private UserResponse follower;
    private UserResponse followee;

    public static FollowResponse from(Follow follow) {
        return FollowResponse.builder()
                .followerId(follow.getFollowerId())
                .followeeId(follow.getFolloweeId())
                .createdAt(follow.getCreatedAt())
                .build();
    }
}
