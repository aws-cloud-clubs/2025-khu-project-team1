package com.khu.acc.newsfeed.newsfeed.interfaces;

import com.khu.acc.newsfeed.common.dto.ApiResponse;
import com.khu.acc.newsfeed.newsfeed.application.NewsFeedService;
import com.khu.acc.newsfeed.post.post.interfaces.dto.PostResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
@Tag(name = "News Feed", description = "뉴스 피드 API")
public class NewsFeedController {

    private final NewsFeedService newsFeedService;

    @GetMapping
    @Operation(summary = "뉴스 피드 조회", description = "사용자의 개인화된 뉴스 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> getNewsFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<PostResponse> feed = newsFeedService.getUserNewsFeed(userId, scrollRequest);
        
        log.debug("NewsFeed retrieved for user: {}, items: {}", userId, feed.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("News feed retrieved successfully", feed));
    }

    @GetMapping("/personalized")
    @Operation(summary = "개인화 피드 조회", description = "사용자 관심사 기반 개인화된 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> getPersonalizedFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<PostResponse> feed = newsFeedService.getPersonalizedFeed(userId, scrollRequest);
        
        log.debug("Personalized feed retrieved for user: {}, items: {}", userId, feed.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Personalized feed retrieved successfully", feed));
    }

    @PostMapping("/refresh")
    @Operation(summary = "피드 새로고침", description = "사용자의 피드 캐시를 새로고침합니다.")
    public ResponseEntity<ApiResponse<ScrollResponse<PostResponse>>> refreshFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        ScrollRequest scrollRequest = ScrollRequest.of(cursor, limit);
        
        ScrollResponse<PostResponse> feed = newsFeedService.refreshNewsFeed(userId, scrollRequest);
        
        log.info("Feed refreshed for user: {}, items: {}", userId, feed.getContent().size());
        return ResponseEntity.ok(ApiResponse.success("Feed refreshed successfully", feed));
    }

    @GetMapping("/stats")
    @Operation(summary = "피드 통계 조회", description = "사용자의 뉴스피드 통계를 조회합니다.")
    public ResponseEntity<ApiResponse<NewsFeedService.NewsFeedStats>> getFeedStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        String userId = extractUserIdFromUserDetails(userDetails);
        NewsFeedService.NewsFeedStats stats = newsFeedService.getUserFeedStats(userId);
        
        return ResponseEntity.ok(ApiResponse.success("Feed stats retrieved successfully", stats));
    }

    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        return userDetails.getUsername(); // 임시로 username을 사용
    }
}