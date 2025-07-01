package com.khu.acc.newsfeed.controller;

import com.khu.acc.newsfeed.dto.ApiResponse;
import com.khu.acc.newsfeed.dto.PostResponse;
import com.khu.acc.newsfeed.service.NewsFeedService;
import com.khu.acc.newsfeed.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@Tag(name = "News Feed", description = "개인화 뉴스 피드 API")
public class NewsFeedController {

    private final NewsFeedService newsFeedService;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "개인화 뉴스 피드 조회",
            description = "사용자의 팔로잉, 관심사, 활동 패턴을 기반으로 개인화된 뉴스 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getNewsFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        try {
            String userId = extractUserIdFromUserDetails(userDetails);
            log.info("Getting personalized news feed for user: {}", userId);

            List<PostResponse> posts = newsFeedService.getPersonalizedNewsFeed(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("개인화 피드 조회 성공 (%d개 포스트)", posts.size()),
                    posts));

        } catch (Exception e) {
            log.error("Error getting news feed", e);
            return ResponseEntity.ok(ApiResponse.error("피드 조회 중 오류가 발생했습니다.", null));
        }
    }

    @GetMapping("/personalized")
    @Operation(summary = "관심사 기반 개인화 피드",
            description = "사용자의 관심사(interests) 태그를 기반으로 개인화된 피드를 조회합니다.")
    public ResponseEntity<ApiResponse<List<PostResponse>>> getPersonalizedFeed(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {

        try {
            String userId = extractUserIdFromUserDetails(userDetails);
            log.info("Getting interest-based feed for user: {}", userId);

            List<PostResponse> posts = newsFeedService.getInterestBasedFeed(userId, pageable);

            return ResponseEntity.ok(ApiResponse.success(
                    String.format("관심사 기반 피드 조회 성공 (%d개 포스트)", posts.size()),
                    posts));

        } catch (Exception e) {
            log.error("Error getting personalized feed", e);
            return ResponseEntity.ok(ApiResponse.error("관심사 피드 조회 중 오류가 발생했습니다.", null));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "피드 캐시 새로고침",
            description = "사용자의 개인화 피드 캐시를 새로고침합니다.")
    public ResponseEntity<ApiResponse<Void>> refreshFeed(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String userId = extractUserIdFromUserDetails(userDetails);
            log.info("Refreshing feed cache for user: {}", userId);

            newsFeedService.invalidateUserFeedCache(userId);

            return ResponseEntity.ok(ApiResponse.success("피드 캐시가 새로고침되었습니다.", null));

        } catch (Exception e) {
            log.error("Error refreshing feed cache", e);
            return ResponseEntity.ok(ApiResponse.error("피드 새로고침 중 오류가 발생했습니다.", null));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "피드 통계 조회",
            description = "사용자의 피드 관련 통계 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<NewsFeedService.FeedStats>> getFeedStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            String userId = extractUserIdFromUserDetails(userDetails);
            log.info("Getting feed stats for user: {}", userId);

            NewsFeedService.FeedStats stats = newsFeedService.getUserFeedStats(userId);

            return ResponseEntity.ok(ApiResponse.success("피드 통계 조회 성공", stats));

        } catch (Exception e) {
            log.error("Error getting feed stats", e);
            return ResponseEntity.ok(ApiResponse.error("피드 통계 조회 중 오류가 발생했습니다.", null));
        }
    }

    @PostMapping("/refresh-all")
    @Operation(summary = "전체 피드 캐시 새로고침",
            description = "모든 사용자의 피드 캐시를 새로고침합니다. (관리자용)")
    public ResponseEntity<ApiResponse<Void>> refreshAllFeedCache() {

        try {
            log.info("Refreshing all feed cache");
            newsFeedService.refreshAllFeedCache();

            return ResponseEntity.ok(ApiResponse.success("전체 피드 캐시가 새로고침되었습니다.", null));

        } catch (Exception e) {
            log.error("Error refreshing all feed cache", e);
            return ResponseEntity.ok(ApiResponse.error("전체 피드 새로고침 중 오류가 발생했습니다.", null));
        }
    }

    @GetMapping("/algorithm-info")
    @Operation(summary = "개인화 알고리즘 정보",
            description = "현재 적용중인 개인화 알고리즘의 가중치 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PersonalizationAlgorithmInfo>> getAlgorithmInfo() {

        PersonalizationAlgorithmInfo info = new PersonalizationAlgorithmInfo(
                "관심사: 40%, 참여도: 30%, 최신성: 20%, 작성자 관계: 10%",
                "팔로잉 기반 + 관심사 태그 매칭 + 활동 패턴 분석",
                "Redis 캐시 30분, 개인화 데이터 캐시 1시간"
        );

        return ResponseEntity.ok(ApiResponse.success("알고리즘 정보 조회 성공", info));
    }

    /**
     * UserDetails에서 사용자 ID 추출
     */
    private String extractUserIdFromUserDetails(UserDetails userDetails) {
        // 실제 구현에서는 UserDetails에서 실제 사용자 ID를 추출해야 합니다.
        // 현재는 username을 기반으로 사용자를 찾습니다.
        return userService.findByUsername(userDetails.getUsername())
                .map(user -> user.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userDetails.getUsername()));
    }

    /**
     * 개인화 알고리즘 정보 DTO
     */
    public record PersonalizationAlgorithmInfo(
            String weights,
            String algorithm,
            String caching
    ) {}
}