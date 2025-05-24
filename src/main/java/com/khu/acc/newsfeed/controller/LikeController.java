package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.LikeResponse;
import com.khu.acc.newsfeed.service.LikeService;
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
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Tag(name = "Like Management", description = "좋아요 관리 API")
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요", description = "포스트에 좋아요를 누릅니다.")
    public ResponseEntity<ApiResponse<LikeResponse>> likePost(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {

    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요 취소", description = "포스트의 좋아요를 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요 목록", description = "포스트에 좋아요를 누른 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LikeResponse>>> getPostLikes(
            @PathVariable String postId,
            @PageableDefault(size = 20) Pageable pageable) {
    }

    @GetMapping("/posts/{postId}/check")
    @Operation(summary = "좋아요 상태 확인", description = "현재 사용자가 포스트에 좋아요를 눌렀는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasLiked(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
    }

    @GetMapping("/posts/{postId}/count")
    @Operation(summary = "포스트 좋아요 수", description = "포스트의 총 좋아요 수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getPostLikesCount(@PathVariable String postId) {
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "사용자 좋아요 목록", description = "사용자가 좋아요를 누른 포스트 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<LikeResponse>>> getUserLikes(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {

    }
}