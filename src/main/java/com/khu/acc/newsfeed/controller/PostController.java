package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.PostCreateRequest;
import com.khu.acc.newsfeed.dto.PostResponse;
import com.khu.acc.newsfeed.dto.PostUpdateRequest;
import com.khu.acc.newsfeed.service.PostService;
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
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "포스트 관리 API")
public class PostController {

    private final PostService postService;

    @PostMapping
    @Operation(summary = "포스트 작성", description = "새로운 포스트를 작성합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

    }

    @GetMapping("/{postId}")
    @Operation(summary = "포스트 조회", description = "포스트 ID로 포스트를 조회합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable String postId) {

    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 포스트 조회", description = "특정 사용자의 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getUserPosts(
            @PathVariable String userId,
            @PageableDefault(size = 20) Pageable pageable) {
    }

    @GetMapping("/trending")
    @Operation(summary = "트렌딩 포스트", description = "인기 있는 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getTrendingPosts(
            @PageableDefault(size = 20) Pageable pageable) {
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "태그별 포스트 조회", description = "특정 태그가 포함된 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByTag(
            @PathVariable String tag,
            @PageableDefault(size = 20) Pageable pageable) {
    }

    @GetMapping("/search")
    @Operation(summary = "포스트 검색", description = "내용으로 포스트를 검색합니다.")
    public ResponseEntity<ApiResponse<List<PostResponse>>> searchPosts(@RequestParam String content) {
    }

    @PutMapping("/{postId}")
    @Operation(summary = "포스트 수정", description = "포스트를 수정합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable String postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "포스트 삭제", description = "포스트를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String postId,
            @AuthenticationPrincipal UserDetails userDetails) {

    }
}
