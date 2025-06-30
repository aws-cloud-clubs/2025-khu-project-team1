package com.khu.acc.newsfeed.comment.comment.interfaces;

import com.khu.acc.newsfeed.comment.comment.interfaces.dto.CommentCreateRequest;
import com.khu.acc.newsfeed.comment.comment.interfaces.dto.CommentResponse;
import com.khu.acc.newsfeed.comment.comment.application.CommentService;
import com.khu.acc.newsfeed.comment.comment.interfaces.dto.CommentUpdateRequest;
import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.exception.comment.CommentNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment Management", description = "댓글 관리 API")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @Operation(summary = "댓글 작성", description = "포스트에 댓글을 작성합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        Comment comment = commentService.createComment(
                userId,
                request.getPostId(),
                request.getContent(),
                request.getParentCommentId()
        );
        
        CommentResponse response = CommentResponse.from(comment);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", response));
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "댓글 조회", description = "댓글 ID로 댓글을 조회합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(
            @PathVariable String commentId) {
        
        Comment comment = commentService.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));
        
        CommentResponse response = CommentResponse.from(comment);
        
        return ResponseEntity.ok(ApiResponse.success("Comment retrieved successfully", response));
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "포스트 댓글 조회", description = "포스트의 댓글들을 스크롤 페이지로 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<CommentResponse>>> getPostComments(
            @PathVariable String postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        String currentUserId = null; // 비로그인 사용자도 댓글 조회 가능
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<CommentResponse> comments = commentService.getPostComments(postId, scrollRequest, currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("Post comments retrieved successfully", comments));
    }

    @GetMapping("/posts/{postId}/top-level")
    @Operation(summary = "최상위 댓글 조회", description = "포스트의 최상위 댓글들만 조회합니다. (/posts/{postId} 엔드포인트와 동일)")
    public ResponseEntity<ApiResponse<ScrollResponse<CommentResponse>>> getTopLevelComments(
            @PathVariable String postId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        // 최상위 댓글만 조회하는 것은 getPostComments와 동일
        String currentUserId = null; // 비로그인 사용자도 댓글 조회 가능
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        ScrollResponse<CommentResponse> comments = commentService.getPostComments(postId, scrollRequest, currentUserId);
        
        return ResponseEntity.ok(ApiResponse.success("Top level comments retrieved successfully", comments));
    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "대댓글 조회", description = "댓글의 대댓글들을 조회합니다. (더보기 방식)")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(
            @PathVariable String commentId,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        List<CommentResponse> replies = commentService.getCommentReplies(commentId, offset, limit);
        
        return ResponseEntity.ok(ApiResponse.success("Comment replies retrieved successfully", replies));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        Comment updatedComment = commentService.updateComment(commentId, userId, request.getContent());
        CommentResponse response = CommentResponse.from(updatedComment);
        
        return ResponseEntity.ok(ApiResponse.success("Comment updated successfully", response));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다. (소프트 삭제 - '삭제된 댓글입니다' 표시)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        commentService.deleteComment(commentId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Comment deleted successfully", null));
    }

    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername(); // 임시로 username을 사용
    }
}