package com.khu.acc.newsfeed.post.postlike.interfaces;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.util.SecurityContextUtil;
import com.khu.acc.newsfeed.post.postlike.application.LikeService;
import com.khu.acc.newsfeed.post.postlike.domain.Like;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Tag(name = "Like Management", description = "좋아요 관리 API")
public class LikeController {

    private final LikeService likeService;
    private final SecurityContextUtil securityContextUtil;

    @PostMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요", description = "포스트에 좋아요를 누릅니다.")
    public ResponseEntity<ApiResponse<LikeResponse>> likePost(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = securityContextUtil.getCurrentUserId();
        Like like = likeService.likePost(postId, userId);
        LikeResponse response = LikeResponse.from(like);
        
        return ResponseEntity.ok(ApiResponse.success("Post liked successfully", response));
    }

    @DeleteMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요 취소", description = "포스트의 좋아요를 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> unlikePost(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = securityContextUtil.getCurrentUserId();
        likeService.unlikePost(postId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Post unliked successfully", null));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "포스트 좋아요 목록", description = "포스트에 좋아요를 누른 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<LikeResponse>>> getPostLikes(
            @PathVariable String postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<Like> likes = likeService.getPostLikes(postId, scrollRequest);
        
        List<LikeResponse> responses = likes.getContent().stream()
                .map(LikeResponse::from)
                .toList();
        
        ScrollResponse<LikeResponse> response = ScrollResponse.of(
                responses,
                likes.getNextCursor(),
                likes.getPreviousCursor(),
                likes.isHasNext(),
                likes.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Post likes retrieved successfully", response));
    }

    @GetMapping("/posts/{postId}/check")
    @Operation(summary = "좋아요 상태 확인", description = "현재 사용자가 포스트에 좋아요를 눌렀는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasLiked(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = securityContextUtil.getCurrentUserId();
        boolean hasLiked = likeService.hasUserLikedPost(postId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Like status retrieved successfully", hasLiked));
    }

    @GetMapping("/posts/{postId}/count")
    @Operation(summary = "포스트 좋아요 수", description = "포스트의 총 좋아요 수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getPostLikesCount(@PathVariable String postId) {
        
        Long likesCount = likeService.getPostLikesCount(postId);
        
        return ResponseEntity.ok(ApiResponse.success("Post likes count retrieved successfully", likesCount));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "사용자 좋아요 목록", description = "사용자가 좋아요를 누른 포스트 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<LikeResponse>>> getUserLikes(
            @PathVariable String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<Like> likes = likeService.getUserLikes(userId, scrollRequest);
        
        List<LikeResponse> responses = likes.getContent().stream()
                .map(LikeResponse::from)
                .toList();
        
        ScrollResponse<LikeResponse> response = ScrollResponse.of(
                responses,
                likes.getNextCursor(),
                likes.getPreviousCursor(),
                likes.isHasNext(),
                likes.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("User likes retrieved successfully", response));
    }
    
}