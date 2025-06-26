package com.khu.acc.newsfeed.controller;


import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.FollowResponse;
import com.khu.acc.newsfeed.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/follows")
@RequiredArgsConstructor
@Tag(name = "Follow Management", description = "팔로우 관리 API")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followeeId}")
    @Operation(summary = "팔로우", description = "사용자를 팔로우합니다.")
    public ResponseEntity<ApiResponse<FollowResponse>> followUser(
            @PathVariable String followeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<FollowResponse>builder().build());
    }

    @DeleteMapping("/{followeeId}")
    @Operation(summary = "언팔로우", description = "사용자를 언팔로우합니다.")
    public ResponseEntity<ApiResponse<Void>> unfollowUser(
            @PathVariable String followeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<Void>builder().build());
    }

    @GetMapping("/{userId}/following")
    @Operation(summary = "팔로잉 목록", description = "사용자가 팔로우하는 사람들의 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<FollowResponse>>> getFollowing(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<FollowResponse>>builder().build());
    }

    @GetMapping("/{userId}/followers")
    @Operation(summary = "팔로워 목록", description = "사용자를 팔로우하는 사람들의 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<FollowResponse>>> getFollowers(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<Page<FollowResponse>>builder().build());
    }

    @GetMapping("/{followeeId}/is-following")
    @Operation(summary = "팔로우 상태 확인", description = "현재 사용자가 특정 사용자를 팔로우하고 있는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> isFollowing(
            @PathVariable String followeeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<Boolean>builder().build());
    }

    @GetMapping("/{userId}/stats")
    @Operation(summary = "팔로우 통계", description = "사용자의 팔로워/팔로잉 수를 조회합니다.")
    public ResponseEntity<ApiResponse<FollowStats>> getFollowStats(@PathVariable String userId) {
        return ResponseEntity.ok(ApiResponse.<FollowStats>builder().build());
    }

    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername(); // 임시로 username을 사용
    }

    public record FollowStats(Long followersCount, Long followingCount) {}
}
