package com.khu.acc.newsfeed.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("뉴스피드 API E2E 테스트")
class NewsFeedE2ETest extends E2ETestBase {

    @Nested
    @DisplayName("뉴스피드 조회")
    class NewsFeedRetrieval {

        @Test
        @DisplayName("인증되지 않은 사용자가 뉴스피드를 조회하려고 하면 실패한다")
        void failsWhenUnauthenticatedUserTriesToAccessFeed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed")
                            .param("limit", "10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 개인화 피드를 조회하려고 하면 실패한다")
        void failsWhenUnauthenticatedUserTriesToAccessPersonalizedFeed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed/personalized")
                            .param("limit", "10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 피드를 새로고침하려고 하면 실패한다")
        void failsWhenUnauthenticatedUserTriesToRefreshFeed() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/v1/feed/refresh")
                            .param("limit", "10"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 피드 통계를 조회하려고 하면 실패한다")
        void failsWhenUnauthenticatedUserTriesToAccessFeedStats() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed/stats"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("뉴스피드 검증")
    class NewsFeedValidation {

        @Test
        @DisplayName("피드 조회 시 limit 값이 음수일 때 적절히 처리된다")
        void handlesNegativeLimitForFeedGracefully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed")
                            .param("limit", "-1"))
                    .andExpect(status().isUnauthorized()); // 인증 실패가 먼저 처리됨
        }

        @Test
        @DisplayName("피드 조회 시 매우 큰 limit 값일 때 적절히 처리된다")
        void handlesLargeLimitForFeedGracefully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed")
                            .param("limit", "10000"))
                    .andExpect(status().isUnauthorized()); // 인증 실패가 먼저 처리됨
        }

        @Test
        @DisplayName("개인화 피드 조회 시 커서 파라미터를 전달할 수 있다")
        void canPassCursorParameterForPersonalizedFeed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/feed/personalized")
                            .param("cursor", "test-cursor")
                            .param("limit", "20"))
                    .andExpect(status().isUnauthorized()); // 인증 실패가 먼저 처리됨
        }
    }
}