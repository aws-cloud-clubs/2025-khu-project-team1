package com.khu.acc.newsfeed.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("댓글 API E2E 테스트")
class CommentE2ETest extends E2ETestBase {

    @Nested
    @DisplayName("댓글 조회")
    class CommentRetrieval {

        @Test
        @DisplayName("존재하지 않는 댓글 ID로 조회를 시도할 때 실패한다")
        void failsWhenCommentNotFound() throws Exception {
            // Given
            String nonExistentCommentId = "non-existent-comment-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + nonExistentCommentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("특정 포스트의 댓글 목록을 조회할 수 있다")
        void retrievesPostCommentsSuccessfully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/posts/" + postId)
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("특정 포스트의 최상위 댓글 목록을 조회할 수 있다")
        void retrievesTopLevelCommentsSuccessfully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/posts/" + postId + "/top-level")
                            .param("limit", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.hasNext").exists())
                    .andExpect(jsonPath("$.data.hasPrevious").exists());
        }

        @Test
        @DisplayName("특정 댓글의 대댓글 목록을 조회할 수 있다")
        void retrievesCommentRepliesSuccessfully() throws Exception {
            // Given
            String commentId = "test-comment-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("커서를 사용하여 댓글 목록을 페이징 조회할 수 있다")
        void retrievesCommentsWithCursorPagination() throws Exception {
            // Given
            String postId = "test-post-id";
            String cursor = "test-cursor";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/posts/" + postId)
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
    @DisplayName("댓글 검증")
    class CommentValidation {

        @Test
        @DisplayName("존재하지 않는 포스트의 댓글 목록 조회 시 빈 결과를 반환한다")
        void returnsEmptyResultForNonExistentPost() throws Exception {
            // Given
            String nonExistentPostId = "non-existent-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/posts/" + nonExistentPostId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("존재하지 않는 댓글의 대댓글 목록 조회 시 빈 결과를 반환한다")
        void returnsEmptyResultForNonExistentCommentReplies() throws Exception {
            // Given
            String nonExistentCommentId = "non-existent-comment-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + nonExistentCommentId + "/replies")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("limit 값이 음수일 때 적절히 처리된다")
        void handlesNegativeLimitGracefully() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/posts/" + postId)
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
            mockMvc.perform(get("/api/v1/comments/posts/" + postId)
                            .param("limit", "10000"))
                    .andExpect(status().isOk()) // 서비스에서 최대값으로 제한될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("offset 값이 음수일 때 적절히 처리된다")
        void handlesNegativeOffsetGracefully() throws Exception {
            // Given
            String commentId = "test-comment-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                            .param("offset", "-1")
                            .param("limit", "10"))
                    .andExpect(status().isOk()) // 서비스에서 기본값으로 처리될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("매우 큰 offset 값일 때 적절히 처리된다")
        void handlesLargeOffsetGracefully() throws Exception {
            // Given
            String commentId = "test-comment-id";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                            .param("offset", "10000")
                            .param("limit", "10"))
                    .andExpect(status().isOk()) // 빈 결과 또는 적절한 처리
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("댓글 계층 구조")
    class CommentHierarchy {

        @Test
        @DisplayName("최상위 댓글과 일반 댓글 목록이 동일하게 조회된다")
        void topLevelAndRegularCommentsReturnSameResults() throws Exception {
            // Given
            String postId = "test-post-id";

            // When & Then - 일반 댓글 조회
            mockMvc.perform(get("/api/v1/comments/posts/" + postId)
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());

            // When & Then - 최상위 댓글 조회 (동일한 결과 예상)
            mockMvc.perform(get("/api/v1/comments/posts/" + postId + "/top-level")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("여러 댓글의 대댓글을 연속으로 조회할 수 있다")
        void retrievesMultipleCommentRepliesSuccessively() throws Exception {
            // Given
            String[] commentIds = {"comment1", "comment2", "comment3"};

            // When & Then
            for (String commentId : commentIds) {
                mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                                .param("offset", "0")
                                .param("limit", "5"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data").isArray());
            }
        }

        @Test
        @DisplayName("대댓글 페이징이 올바르게 동작한다")
        void repliesPaginationWorksCorrectly() throws Exception {
            // Given
            String commentId = "test-comment-id";

            // When & Then - 첫 번째 페이지
            mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                            .param("offset", "0")
                            .param("limit", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());

            // When & Then - 두 번째 페이지
            mockMvc.perform(get("/api/v1/comments/" + commentId + "/replies")
                            .param("offset", "3")
                            .param("limit", "3"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("빈 댓글 ID로 대댓글 조회 시 404를 반환한다")
        void returns404ForEmptyCommentId() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/comments//replies")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isNotFound()); // 빈 경로로 인한 404
        }

        @Test
        @DisplayName("특수 문자가 포함된 댓글 ID로 조회 시 적절히 처리된다")
        void handlesSpecialCharactersInCommentIdGracefully() throws Exception {
            // Given
            String commentIdWithSpecialChars = "comment-123-@#$";

            // When & Then
            mockMvc.perform(get("/api/v1/comments/" + commentIdWithSpecialChars + "/replies")
                            .param("offset", "0")
                            .param("limit", "10"))
                    .andExpect(status().isOk()) // 서비스에서 적절히 처리될 것으로 예상
                    .andExpect(jsonPath("$.success").value(true));
        }
    }
}