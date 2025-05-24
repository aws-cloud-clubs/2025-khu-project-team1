package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.CommentCreateRequest;
import com.khu.acc.newsfeed.dto.CommentResponse;
import com.khu.acc.newsfeed.model.Comment;
import com.khu.acc.newsfeed.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Optional;

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

    }

    @GetMapping("/{commentId}")
    @Operation(summary = "댓글 조회", description = "댓글 ID로 댓글을 조회합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable String commentId) {
    }

    @GetMapping("/posts/{postId}")
    @Operation(summary = "포스트 댓글 조회", description = "포스트의 댓글들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getPostComments(@PathVariable String postId) {
    }

    @GetMapping("/posts/{postId}/top-level")
    @Operation(summary = "최상위 댓글 조회", description = "포스트의 최상위 댓글들만 조회합니다.")
    public ResponseEntity<ApiResponse<Page<CommentResponse>>> getTopLevelComments(
            @PathVariable String postId,
            @PageableDefault(size = 20) Pageable pageable) {

    }

    @GetMapping("/{commentId}/replies")
    @Operation(summary = "대댓글 조회", description = "댓글의 대댓글들을 조회합니다.")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getReplies(@PathVariable String commentId) {
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "댓글 수정", description = "댓글을 수정합니다.")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable String commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal UserDetails userDetails) {

    }

    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername(); // 임시로 username을 사용
    }
}