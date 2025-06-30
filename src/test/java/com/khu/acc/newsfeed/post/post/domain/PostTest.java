package com.khu.acc.newsfeed.post.post.domain;

import com.khu.acc.newsfeed.post.post.application.command.PostCreateCommand;
import com.khu.acc.newsfeed.post.post.application.command.PostUpdateCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Post 도메인 단위테스트")
class PostTest {

    @Nested
    @DisplayName("Post 생성 시")
    class PostCreationTest {

        @Nested
        @DisplayName("유효한 PostCreateCommand가 주어졌을 때")
        class WithValidPostCreateCommand {

            @Test
            @DisplayName("기본 필드들이 올바르게 설정된 Post를 생성해야 한다")
            void shouldCreatePostWithBasicFields() {
                // Given
                String userId = "user123";
                String content = "테스트 포스트 내용";
                PostCreateCommand command = PostCreateCommand.of(userId, content);

                // When
                Post post = Post.from(command);

                // Then
                assertThat(post.getPostId()).isNotNull().startsWith("post_");
                assertThat(post.getUserId()).isEqualTo(userId);
                assertThat(post.getContent()).isEqualTo(content);
                assertThat(post.getIsActive()).isTrue();
                assertThat(post.getLikesCount()).isEqualTo(0L);
                assertThat(post.getCommentsCount()).isEqualTo(0L);
                assertThat(post.getCreatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
                assertThat(post.getUpdatedAt()).isNotNull().isBeforeOrEqualTo(Instant.now());
            }

            @Test
            @DisplayName("이미지 URL이 포함된 Post를 생성해야 한다")
            void shouldCreatePostWithImageUrls() {
                // Given
                String userId = "user123";
                String content = "이미지가 포함된 포스트";
                List<String> imageUrls = List.of(
                    "https://example.com/image1.jpg",
                    "https://example.com/image2.jpg"
                );
                PostCreateCommand command = PostCreateCommand.withImages(userId, content, imageUrls);

                // When
                Post post = Post.from(command);

                // Then
                assertThat(post.getImageUrls()).containsExactlyInAnyOrderElementsOf(imageUrls);
            }

            @Test
            @DisplayName("태그가 포함된 Post를 생성해야 한다")
            void shouldCreatePostWithTags() {
                // Given
                String userId = "user123";
                String content = "태그가 포함된 포스트";
                Set<String> tags = Set.of("개발", "자바", "스프링");
                PostCreateCommand command = PostCreateCommand.withTags(userId, content, tags);

                // When
                Post post = Post.from(command);

                // Then
                assertThat(post.getTags()).containsExactlyInAnyOrderElementsOf(tags);
            }

            @Test
            @DisplayName("위치 정보가 포함된 Post를 생성해야 한다")
            void shouldCreatePostWithLocation() {
                // Given
                String userId = "user123";
                String content = "위치가 포함된 포스트";
                String location = "서울시 강남구";
                PostCreateCommand command = PostCreateCommand.builder()
                    .userId(userId)
                    .content(content)
                    .location(location)
                    .build();

                // When
                Post post = Post.from(command);

                // Then
                assertThat(post.getLocation()).isEqualTo(location);
            }
        }

        @Nested
        @DisplayName("null 값들이 포함된 PostCreateCommand가 주어졌을 때")
        class WithNullValuesInCommand {

            @Test
            @DisplayName("빈 컬렉션으로 초기화된 Post를 생성해야 한다")
            void shouldCreatePostWithEmptyCollections() {
                // Given
                PostCreateCommand command = PostCreateCommand.builder()
                    .userId("user123")
                    .content("내용")
                    .imageUrls(null)
                    .tags(null)
                    .build();

                // When
                Post post = Post.from(command);

                // Then
                assertThat(post.getImageUrls()).isNotNull().isEmpty();
                assertThat(post.getTags()).isNotNull().isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("Post 업데이트 시")
    class PostUpdateTest {

        private Post createTestPost() {
            PostCreateCommand command = PostCreateCommand.builder()
                .userId("user123")
                .content("원본 내용")
                .tags(Set.of("원본태그"))
                .location("원본위치")
                .build();
            return Post.from(command);
        }

        @Nested
        @DisplayName("유효한 PostUpdateCommand가 주어졌을 때")
        class WithValidPostUpdateCommand {

            @Test
            @DisplayName("내용만 업데이트해야 한다")
            void shouldUpdateContentOnly() {
                // Given
                Post post = createTestPost();
                String originalLocation = post.getLocation();
                Set<String> originalTags = post.getTags();
                Instant originalUpdatedAt = post.getUpdatedAt();
                
                String newContent = "새로운 내용";
                PostUpdateCommand command = PostUpdateCommand.withContent(
                    post.getPostId(), post.getUserId(), newContent
                );

                // When
                post.updateFrom(command);

                // Then
                assertThat(post.getContent()).isEqualTo(newContent);
                assertThat(post.getLocation()).isEqualTo(originalLocation);
                assertThat(post.getTags()).isEqualTo(originalTags);
                assertThat(post.getUpdatedAt()).isAfter(originalUpdatedAt);
            }

            @Test
            @DisplayName("태그만 업데이트해야 한다")
            void shouldUpdateTagsOnly() {
                // Given
                Post post = createTestPost();
                String originalContent = post.getContent();
                String originalLocation = post.getLocation();
                
                Set<String> newTags = Set.of("새태그1", "새태그2");
                PostUpdateCommand command = PostUpdateCommand.withTags(
                    post.getPostId(), post.getUserId(), newTags
                );

                // When
                post.updateFrom(command);

                // Then
                assertThat(post.getTags()).isEqualTo(newTags);
                assertThat(post.getContent()).isEqualTo(originalContent);
                assertThat(post.getLocation()).isEqualTo(originalLocation);
            }

            @Test
            @DisplayName("위치만 업데이트해야 한다")
            void shouldUpdateLocationOnly() {
                // Given
                Post post = createTestPost();
                String originalContent = post.getContent();
                Set<String> originalTags = post.getTags();
                
                String newLocation = "새로운 위치";
                PostUpdateCommand command = PostUpdateCommand.withLocation(
                    post.getPostId(), post.getUserId(), newLocation
                );

                // When
                post.updateFrom(command);

                // Then
                assertThat(post.getLocation()).isEqualTo(newLocation);
                assertThat(post.getContent()).isEqualTo(originalContent);
                assertThat(post.getTags()).isEqualTo(originalTags);
            }

            @Test
            @DisplayName("모든 필드를 업데이트해야 한다")
            void shouldUpdateAllFields() {
                // Given
                Post post = createTestPost();
                Instant originalUpdatedAt = post.getUpdatedAt();
                
                String newContent = "새로운 내용";
                Set<String> newTags = Set.of("새태그");
                String newLocation = "새로운 위치";
                
                PostUpdateCommand command = PostUpdateCommand.builder()
                    .postId(post.getPostId())
                    .userId(post.getUserId())
                    .content(newContent)
                    .tags(newTags)
                    .location(newLocation)
                    .build();

                // When
                post.updateFrom(command);

                // Then
                assertThat(post.getContent()).isEqualTo(newContent);
                assertThat(post.getTags()).isEqualTo(newTags);
                assertThat(post.getLocation()).isEqualTo(newLocation);
                assertThat(post.getUpdatedAt()).isAfter(originalUpdatedAt);
            }
        }

        @Nested
        @DisplayName("null 값이 포함된 PostUpdateCommand가 주어졌을 때")
        class WithNullValuesInUpdateCommand {

            @Test
            @DisplayName("null 필드는 업데이트하지 않아야 한다")
            void shouldNotUpdateNullFields() {
                // Given
                Post post = createTestPost();
                String originalContent = post.getContent();
                Set<String> originalTags = post.getTags();
                String originalLocation = post.getLocation();
                
                PostUpdateCommand command = PostUpdateCommand.builder()
                    .postId(post.getPostId())
                    .userId(post.getUserId())
                    .content(null)
                    .tags(null)
                    .location(null)
                    .build();

                // When
                post.updateFrom(command);

                // Then
                assertThat(post.getContent()).isEqualTo(originalContent);
                assertThat(post.getTags()).isEqualTo(originalTags);
                assertThat(post.getLocation()).isEqualTo(originalLocation);
            }
        }
    }

    @Nested
    @DisplayName("Post 카운터 조작 시")
    class PostCounterTest {

        private Post createTestPost() {
            return Post.from(PostCreateCommand.of("user123", "테스트 내용"));
        }

        @Nested
        @DisplayName("좋아요 카운트 증가 시")
        class IncrementLikesCount {

            @Test
            @DisplayName("좋아요 수가 1 증가해야 한다")
            void shouldIncrementLikesCount() {
                // Given
                Post post = createTestPost();
                Long originalCount = post.getLikesCount();

                // When
                post.incrementLikesCount();

                // Then
                assertThat(post.getLikesCount()).isEqualTo(originalCount + 1);
            }

            @Test
            @DisplayName("null인 좋아요 수도 0에서 1로 증가해야 한다")
            void shouldIncrementFromNullLikesCount() {
                // Given
                Post post = createTestPost();
                post.setLikesCount(null);

                // When
                post.incrementLikesCount();

                // Then
                assertThat(post.getLikesCount()).isEqualTo(1L);
            }
        }

        @Nested
        @DisplayName("좋아요 카운트 감소 시")
        class DecrementLikesCount {

            @Test
            @DisplayName("좋아요 수가 1 감소해야 한다")
            void shouldDecrementLikesCount() {
                // Given
                Post post = createTestPost();
                post.setLikesCount(5L);

                // When
                post.decrementLikesCount();

                // Then
                assertThat(post.getLikesCount()).isEqualTo(4L);
            }

            @Test
            @DisplayName("0보다 작아지지 않아야 한다")
            void shouldNotGoNegative() {
                // Given
                Post post = createTestPost();
                post.setLikesCount(0L);

                // When
                post.decrementLikesCount();

                // Then
                assertThat(post.getLikesCount()).isEqualTo(0L);
            }

            @Test
            @DisplayName("null인 좋아요 수는 0을 유지해야 한다")
            void shouldKeepZeroFromNullLikesCount() {
                // Given
                Post post = createTestPost();
                post.setLikesCount(null);

                // When
                post.decrementLikesCount();

                // Then
                assertThat(post.getLikesCount()).isEqualTo(0L);
            }
        }

        @Nested
        @DisplayName("댓글 카운트 증가 시")
        class IncrementCommentsCount {

            @Test
            @DisplayName("댓글 수가 1 증가해야 한다")
            void shouldIncrementCommentsCount() {
                // Given
                Post post = createTestPost();
                Long originalCount = post.getCommentsCount();

                // When
                post.incrementCommentsCount();

                // Then
                assertThat(post.getCommentsCount()).isEqualTo(originalCount + 1);
            }
        }

        @Nested
        @DisplayName("댓글 카운트 감소 시")
        class DecrementCommentsCount {

            @Test
            @DisplayName("댓글 수가 1 감소해야 한다")
            void shouldDecrementCommentsCount() {
                // Given
                Post post = createTestPost();
                post.setCommentsCount(3L);

                // When
                post.decrementCommentsCount();

                // Then
                assertThat(post.getCommentsCount()).isEqualTo(2L);
            }

            @Test
            @DisplayName("0보다 작아지지 않아야 한다")
            void shouldNotGoNegative() {
                // Given
                Post post = createTestPost();
                post.setCommentsCount(0L);

                // When
                post.decrementCommentsCount();

                // Then
                assertThat(post.getCommentsCount()).isEqualTo(0L);
            }
        }
    }

    @Nested
    @DisplayName("Post 활성 상태 확인 시")
    class PostActiveStatusTest {

        @Nested
        @DisplayName("isActive가 true일 때")
        class WhenIsActiveIsTrue {

            @Test
            @DisplayName("활성 상태로 판단해야 한다")
            void shouldReturnTrue() {
                // Given
                Post post = Post.from(PostCreateCommand.of("user123", "내용"));
                post.setIsActive(true);

                // When
                boolean result = post.isActiveStatus();

                // Then
                assertThat(result).isTrue();
            }
        }

        @Nested
        @DisplayName("isActive가 false일 때")
        class WhenIsActiveIsFalse {

            @Test
            @DisplayName("비활성 상태로 판단해야 한다")
            void shouldReturnFalse() {
                // Given
                Post post = Post.from(PostCreateCommand.of("user123", "내용"));
                post.setIsActive(false);

                // When
                boolean result = post.isActiveStatus();

                // Then
                assertThat(result).isFalse();
            }
        }

        @Nested
        @DisplayName("isActive가 null일 때")
        class WhenIsActiveIsNull {

            @Test
            @DisplayName("비활성 상태로 판단해야 한다")
            void shouldReturnFalse() {
                // Given
                Post post = Post.from(PostCreateCommand.of("user123", "내용"));
                post.setIsActive(null);

                // When
                boolean result = post.isActiveStatus();

                // Then
                assertThat(result).isFalse();
            }
        }
    }
}