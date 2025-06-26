package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.PostResponse;
import com.khu.acc.newsfeed.model.Post;
import com.khu.acc.newsfeed.service.NewsFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "News Feed", description = "뉴스 피드 API")
public class NewsFeedController {

    private final NewsFeedService newsFeedService;

    @GetMapping
    @Operation(summary = "뉴스 피드 조회", description = "사용자의 개인화된 뉴스 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getNewsFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<List<PostResponse>>builder().build());
    }

    @GetMapping("/personalized")
    @Operation(summary = "개인화 피드 조회", description = "사용자 관심사 기반 개인화된 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPersonalizedFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.<List<PostResponse>>builder().build());
    }

    @PostMapping("/refresh")
    @Operation(summary = "피드 새로고침", description = "사용자의 피드 캐시를 새로고침합니다.")
    public ResponseEntity<ApiResponse<Void>> refreshFeed(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.<Void>builder().build());
    }

    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername(); // 임시로 username을 사용
    }
}