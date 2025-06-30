package com.khu.acc.newsfeed.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("좋아요 API E2E 테스트")
class LikeE2ETest extends E2ETestBase {

    @Nested
    @DisplayName("포스트 좋아요 조회")
    class PostLikeRetrieval {

        @Test
        @DisplayName("포스트의 좋아요 목록을 조회할 수 있다")
        void retrievesPostLikesSuccessfully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("포스트의 좋아요 수를 조회할 수 있다")
        void retrievesPostLikesCountSuccessfully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postId + "/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("사용자의 좋아요 목록을 조회할 수 있다")
        void retrievesUserLikesSuccessfully() throws Exception {
            // Given
            String userId = "test-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/users/" + userId)
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("커서를 사용하여 좋아요 목록을 페이징 조회할 수 있다")
        void retrievesLikesWithCursorPagination() throws Exception {
            // Given
            String postId = "test-post-id";
            String cursor = "test-cursor";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postId)
                            .param("cursor", cursor)
                            .param("limit", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.nextCursor").exists())
                    .andExpect(jsonPath("$.data.previousCursor").exists());
        }
    }

    @Nested
    @DisplayName("좋아요 API 검증")
    class LikeValidation {

        @Test
        @DisplayName("존재하지 않는 포스트의 좋아요 목록 조회 시 빈 결과를 반환한다")
        void returnsEmptyResultForNonExistentPost() throws Exception {
            // Given
            String nonExistentPostId = "non-existent-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + nonExistentPostId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("존재하지 않는 포스트의 좋아요 수는 0을 반환한다")
        void returnsZeroForNonExistentPostLikesCount() throws Exception {
            // Given
            String nonExistentPostId = "non-existent-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + nonExistentPostId + "/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").value(0));
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 좋아요 목록 조회 시 빈 결과를 반환한다")
        void returnsEmptyResultForNonExistentUser() throws Exception {
            // Given
            String nonExistentUserId = "non-existent-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/users/" + nonExistentUserId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("limit 값이 음수일 때 적절히 처리된다")
        void handlesNegativeLimitGracefully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postId)
                            .param("limit", "-1"))
                    .andExpect(status().isOk()) // 서비스에서 기본값으로 처리될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("매우 큰 limit 값일 때 적절히 처리된다")
        void handlesLargeLimitGracefully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postId)
                            .param("limit", "10000"))
                    .andExpect(status().isOk()) // 서비스에서 최대값으로 제한될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("좋아요 통계")
    class LikeStatistics {

        @Test
        @DisplayName("여러 포스트의 좋아요 수를 연속으로 조회할 수 있다")
        void retrievesMultiplePostLikesCountsSuccessively() throws Exception {
            // Given
            String[] postIds = {"post1", "post2", "post3"};

            // When & Then
            for (String postId : postIds) {
                mockMvc.perform(get("/api/v1/likes/posts/" + postId + "/count"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").isNumber());
            }
        }

        @Test
        @DisplayName("빈 포스트 ID로 좋아요 수 조회 시 404를 반환한다")
        void returns404ForEmptyPostId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts//count"))
                    .andExpect(status().isNotFound()); // 빈 경로로 인한 404
        }

        @Test
        @DisplayName("특수 문자가 포함된 포스트 ID로 조회 시 적절히 처리된다")
        void handlesSpecialCharactersInPostIdGracefully() throws Exception {
            // Given
            String postIdWithSpecialChars = "post-123-@#$";

            // When & Then
            mockMvc.perform(get("/api/v1/likes/posts/" + postIdWithSpecialChars + "/count"))
                    .andExpect(status().isOk()) // 서비스에서 적절히 처리될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}