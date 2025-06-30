package com.khu.acc.newsfeed.post.post.interfaces;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.util.SecurityContextUtil;
import com.khu.acc.newsfeed.post.post.application.command.PostCreateCommand;
import com.khu.acc.newsfeed.post.post.application.command.PostUpdateCommand;
import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.post.image.ImageUploadRequest;
import com.khu.acc.newsfeed.post.image.ImageUploadResponse;
import com.khu.acc.newsfeed.post.image.ImageUploadService;
import com.khu.acc.newsfeed.post.post.application.PostService;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.interfaces.dto.PostCreateRequest;
import com.khu.acc.newsfeed.post.post.interfaces.dto.PostResponse;
import com.khu.acc.newsfeed.post.post.interfaces.dto.PostUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Post Management", description = "포스트 관리 API")
public class PostController {

    private final PostService postService;
    private final ImageUploadService imageUploadService;
    private final SecurityContextUtil securityContextUtil;

    @PostMapping
    @Operation(summary = "포스트 작성", description = "새로운 포스트를 작성합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> createPost(
            @Valid @RequestBody PostCreateRequest request) {
        
        String userId = securityContextUtil.getCurrentUserId();
        PostCreateCommand command = PostCreateCommand.from(request, userId);
        Post post = postService.createPost(command);
        PostResponse response = PostResponse.from(post);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Post created successfully", response));
    }

    @GetMapping("/{postId}")
    @Operation(summary = "포스트 조회", description = "포스트 ID로 포스트를 조회합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable String postId) {
        Optional<Post> postOpt = postService.findById(postId);
        if (postOpt.isEmpty()) {
            throw new PostNotFoundException(postId);
        }
        
        PostResponse response = PostResponse.from(postOpt.get());
        return ResponseEntity.ok(ApiResponse.success("Post retrieved successfully", response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 포스트 조회", description = "특정 사용자의 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> getUserPosts(
            @PathVariable String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<Post> posts = postService.findUserPosts(userId, scrollRequest);
        ScrollResponse<PostResponse> response = ScrollResponse.of(
                posts.getContent().stream().map(PostResponse::from).toList(),
                posts.getNextCursor(),
                posts.getPreviousCursor(),
                posts.isHasNext(),
                posts.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("User posts retrieved successfully", response));
    }

    @GetMapping("/trending")
    @Operation(summary = "트렌딩 포스트", description = "인기 있는 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> getTrendingPosts(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<Post> posts = postService.findTrendingPosts(scrollRequest);
        ScrollResponse<PostResponse> response = ScrollResponse.of(
                posts.getContent().stream().map(PostResponse::from).toList(),
                posts.getNextCursor(),
                posts.getPreviousCursor(),
                posts.isHasNext(),
                posts.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Trending posts retrieved successfully", response));
    }

    @GetMapping("/tag/{tag}")
    @Operation(summary = "태그별 포스트 조회", description = "특정 태그가 포함된 포스트들을 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> getPostsByTag(
            @PathVariable String tag,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<Post> posts = postService.findPostsByTag(tag, scrollRequest);
        ScrollResponse<PostResponse> response = ScrollResponse.of(
                posts.getContent().stream().map(PostResponse::from).toList(),
                posts.getNextCursor(),
                posts.getPreviousCursor(),
                posts.isHasNext(),
                posts.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Posts by tag retrieved successfully", response));
    }

    @GetMapping("/search")
    @Operation(summary = "포스트 검색", description = "내용으로 포스트를 검색합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> searchPosts(
            @RequestParam String content,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<Post> posts = postService.searchPosts(content, scrollRequest);
        ScrollResponse<PostResponse> response = ScrollResponse.of(
                posts.getContent().stream().map(PostResponse::from).toList(),
                posts.getNextCursor(),
                posts.getPreviousCursor(),
                posts.isHasNext(),
                posts.isHasPrevious()
        );
        
        return ResponseEntity.ok(ApiResponse.success("Posts searched successfully", response));
    }

    @PutMapping("/{postId}")
    @Operation(summary = "포스트 수정", description = "포스트를 수정합니다.")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable String postId,
            @Valid @RequestBody PostUpdateRequest request) {
        
        String userId = securityContextUtil.getCurrentUserId();
        PostUpdateCommand command = PostUpdateCommand.from(request, postId, userId);
        Post post = postService.updatePost(command);
        PostResponse response = PostResponse.from(post);
        
        return ResponseEntity.ok(ApiResponse.success("Post updated successfully", response));
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "포스트 삭제", description = "포스트를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deletePost(
            @PathVariable String postId) {
        
        String userId = securityContextUtil.getCurrentUserId();
        postService.deletePost(postId, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Post deleted successfully", null));
    }

    @PostMapping("/images/upload-url")
    @Operation(summary = "이미지 업로드 URL 생성", description = "이미지 업로드를 위한 presigned URL을 생성합니다.")
    public ResponseEntity<ApiResponse<ImageUploadResponse>> getImageUploadUrl(
            @Valid @RequestBody ImageUploadRequest request) {
        
        String userId = securityContextUtil.getCurrentUserId();
        
        ImageUploadResponse response = imageUploadService.generateUploadUrl(request, userId);
        
        return ResponseEntity.ok(ApiResponse.success("Image upload URL generated successfully", response));
    }

}
