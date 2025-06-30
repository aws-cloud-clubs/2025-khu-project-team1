package com.khu.acc.newsfeed.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("포스트 API E2E 테스트")
class PostE2ETest extends E2ETestBase {

    @Nested
    @DisplayName("포스트 조회")
    class PostRetrieval {

        @Test
        @DisplayName("존재하지 않는 포스트 ID로 조회를 시도할 때 실패한다")
        void failsWhenPostNotFound() throws Exception {
            // Given
            String nonExistentPostId = "non-existent-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/" + nonExistentPostId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("특정 사용자의 포스트 목록을 조회할 수 있다")
        void retrievesUserPostsSuccessfully() throws Exception {
            // Given
            String userId = "test-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/user/" + userId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("트렌딩 포스트 목록을 조회할 수 있다")
        void retrievesTrendingPostsSuccessfully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/posts/trending")
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("특정 태그로 포스트를 검색할 수 있다")
        void searchesPostsByTagSuccessfully() throws Exception {
            // Given
            String tag = "테스트";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/tag/" + tag)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists());
        }

        @Test
        @DisplayName("내용으로 포스트를 검색할 수 있다")
        void searchesPostsByContentSuccessfully() throws Exception {
            // Given
            String searchContent = "테스트 내용";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/search")
                            .param("content", searchContent)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists());
        }

        @Test
        @DisplayName("페이징 커서를 사용하여 포스트를 조회할 수 있다")
        void retrievesPostsWithCursorPagination() throws Exception {
            // Given
            String userId = "test-user-id";
            String cursor = "test-cursor";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/user/" + userId)
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
    @DisplayName("포스트 검증")
    class PostValidation {

        @Test
        @DisplayName("잘못된 태그 형식으로 검색할 때 적절히 처리된다")
        void handlesInvalidTagFormatGracefully() throws Exception {
            // Given
            String invalidTag = ""; // 빈 태그

            // When & Then
            mockMvc.perform(get("/api/v1/posts/tag/" + invalidTag)
                            .param("limit", "10"))
                    .andExpect(status().isNotFound()); // 빈 경로로 인한 404
        }

        @Test
        @DisplayName("검색 쿼리가 없을 때 적절한 오류를 반환한다")
        void returnsErrorWhenSearchQueryMissing() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/posts/search")
                            .param("limit", "10"))
                    .andExpect(status().isBadRequest()); // content 파라미터 누락
        }

        @Test
        @DisplayName("limit 값이 음수일 때 적절히 처리된다")
        void handlesNegativeLimitGracefully() throws Exception {
            // Given
            String userId = "test-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/user/" + userId)
                            .param("limit", "-1"))
                    .andExpect(status().isOk()) // 서비스에서 기본값으로 처리될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("매우 큰 limit 값일 때 적절히 처리된다")
        void handlesLargeLimitGracefully() throws Exception {
            // Given
            String userId = "test-user-id";

            // When & Then
            mockMvc.perform(get("/api/v1/posts/user/" + userId)
                            .param("limit", "10000"))
                    .andExpect(status().isOk()) // 서비스에서 최대값으로 제한될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}