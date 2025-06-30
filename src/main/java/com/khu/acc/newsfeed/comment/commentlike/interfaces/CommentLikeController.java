package com.khu.acc.newsfeed.comment.commentlike.interfaces;

import com.khu.acc.newsfeed.comment.commentlike.application.CommentLikeService;
import com.khu.acc.newsfeed.comment.commentlike.domain.CommentLike;
import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
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
@RequestMapping("/api/v1/comment-likes")
@RequiredArgsConstructor
@Tag(name = "Comment Like Management", description = "댓글 좋아요 관리 API")
public class CommentLikeController {

    private final CommentLikeService commentLikeService;

    @PostMapping("/comments/{commentId}")
    @Operation(summary = "댓글 좋아요", description = "댓글에 좋아요를 누릅니다.")
    public ResponseEntity<ApiResponse<CommentLikeResponse>> likeComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        CommentLike commentLike = commentLikeService.likeComment(commentId, userId);
        CommentLikeResponse response = CommentLikeResponse.from(commentLike);
        
        return ResponseEntity.ok(ApiResponse.success("Comment liked successfully", response));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "댓글 좋아요 취소", description = "댓글의 좋아요를 취소합니다.")
    public ResponseEntity<ApiResponse<Void>> unlikeComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        commentLikeService.unlikeComment(commentId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Comment unliked successfully", null));
    }

    @GetMapping("/comments/{commentId}")
    @Operation(summary = "댓글 좋아요 목록", description = "댓글에 좋아요를 누른 사용자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<CommentLikeResponse>>> getCommentLikes(
            @PathVariable String commentId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<CommentLike> likes = commentLikeService.getCommentLikes(commentId, scrollRequest);
        
        List<CommentLikeResponse> responses = likes.getContent().stream()
                .map(CommentLikeResponse::from)
                .toList();
        
        ScrollResponse<CommentLikeResponse> response = ScrollResponse.of(
                responses,
                likes.getNextCursor(),
                likes.getPreviousCursor(),
                likes.isHasNext(),
                likes.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Comment likes retrieved successfully", response));
    }

    @GetMapping("/comments/{commentId}/check")
    @Operation(summary = "댓글 좋아요 상태 확인", description = "현재 사용자가 댓글에 좋아요를 눌렀는지 확인합니다.")
    public ResponseEntity<ApiResponse<Boolean>> hasLiked(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        boolean hasLiked = commentLikeService.hasUserLikedComment(commentId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Comment like status retrieved successfully", hasLiked));
    }

    @GetMapping("/comments/{commentId}/count")
    @Operation(summary = "댓글 좋아요 수", description = "댓글의 총 좋아요 수를 조회합니다.")
    public ResponseEntity<ApiResponse<Long>> getCommentLikesCount(@PathVariable String commentId) {
        
        Long likesCount = commentLikeService.getCommentLikesCount(commentId);
        
        return ResponseEntity.ok(ApiResponse.success("Comment likes count retrieved successfully", likesCount));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "사용자 댓글 좋아요 목록", description = "사용자가 좋아요를 누른 댓글 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<CommentLikeResponse>>> getUserCommentLikes(
            @PathVariable String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<CommentLike> likes = commentLikeService.getUserCommentLikes(userId, scrollRequest);
        
        List<CommentLikeResponse> responses = likes.getContent().stream()
                .map(CommentLikeResponse::from)
                .toList();
        
        ScrollResponse<CommentLikeResponse> response = ScrollResponse.of(
                responses,
                likes.getNextCursor(),
                likes.getPreviousCursor(),
                likes.isHasNext(),
                likes.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("User comment likes retrieved successfully", response));
    }
    
    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername();
    }
}