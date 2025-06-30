package com.khu.acc.newsfeed.post.post.application;

import com.khu.acc.newsfeed.post.image.ImageUploadService;
import com.khu.acc.newsfeed.post.post.application.command.PostCreateCommand;
import com.khu.acc.newsfeed.post.post.application.command.PostUpdateCommand;
import com.khu.acc.newsfeed.common.exception.application.post.PostAccessDeniedException;
import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.common.exception.post.PostErrorCode;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import com.khu.acc.newsfeed.user.application.UserValidator;
import com.khu.acc.newsfeed.user.application.UserStatsService;
import com.khu.acc.newsfeed.common.exception.image.ImageErrorCode;
import com.khu.acc.newsfeed.common.exception.image.ImageValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 포스트 명령(쓰기) 작업 전용 서비스
 * CQRS 패턴의 Command 측면을 담당합니다.
 * 포스트의 생성, 수정, 삭제 등 상태 변경 작업을 처리합니다.
 */
@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final UserValidator userValidator;
    private final UserStatsService userStatsService;
    private final ImageUploadService imageUploadService;

    /**
     * 새로운 포스트를 생성합니다.
     * 
     * @param command 포스트 생성 명령
     * @return 생성된 포스트
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, allEntries = true)
    public Post createPost(PostCreateCommand command) {
        log.debug("Creating post for user: {}", command.getUserId());
        
        // 사용자 존재 확인
        userValidator.validateUserExists(command.getUserId());

        // 이미지 URL 검증
        if (!imageUploadService.validateImageUrls(command.getImageUrls())) {
            throw new ImageValidationException(ImageErrorCode.INVALID_IMAGE_URL);
        }

        // 포스트 생성
        Post post = Post.from(command);
        Post savedPost = postRepository.save(post);

        // 사용자 포스트 카운트 증가
        userStatsService.incrementPostCount(command.getUserId());
        
        log.info("Post created: postId={}, userId={}", 
                savedPost.getPostId(), command.getUserId());

        return savedPost;
    }

    /**
     * 포스트를 수정합니다.
     * 
     * @param command 포스트 수정 명령
     * @return 수정된 포스트
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, allEntries = true)
    public Post updatePost(PostUpdateCommand command) {
        log.debug("Updating post: postId={}, userId={}", command.getPostId(), command.getUserId());
        
        Post post = postRepository.findByPostId(command.getPostId())
                .orElseThrow(() -> new PostNotFoundException(command.getPostId()));

        // 작성자 확인
        if (!post.getUserId().equals(command.getUserId())) {
            throw new PostAccessDeniedException(PostErrorCode.POST_UPDATE_ACCESS_DENIED);
        }

        // 포스트 업데이트
        post.updateFrom(command);
        Post updatedPost = postRepository.save(post);
        
        log.info("Post updated: postId={}", command.getPostId());

        return updatedPost;
    }

    /**
     * 포스트를 삭제합니다 (소프트 삭제).
     * 
     * @param postId 삭제할 포스트 ID
     * @param userId 요청한 사용자 ID
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, allEntries = true)
    public void deletePost(String postId, String userId) {
        log.debug("Deleting post: postId={}, userId={}", postId, userId);
        
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        // 작성자 확인
        if (!post.getUserId().equals(userId)) {
            throw new PostAccessDeniedException(PostErrorCode.POST_DELETE_ACCESS_DENIED);
        }

        // 소프트 삭제
        post.setIsActive(false);
        post.setUpdatedAt(Instant.now());
        postRepository.save(post);

        // 사용자 포스트 카운트 감소
        userStatsService.decrementPostCount(userId);
        
        log.info("Post deleted: postId={}", postId);
    }

    /**
     * 포스트의 좋아요 수를 증가시킵니다.
     * Like 서비스에서 호출되는 메서드입니다.
     * 
     * @param postId 좋아요 수를 증가시킬 포스트 ID
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, key = "#postId")
    public void incrementLikeCount(String postId) {
        log.debug("Incrementing like count for post: {}", postId);
        
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        post.incrementLikesCount();
        postRepository.save(post);
        
        log.debug("Like count incremented for post: {}, newCount={}", 
                postId, post.getLikesCount());
    }

    /**
     * 포스트의 좋아요 수를 감소시킵니다.
     * Like 서비스에서 호출되는 메서드입니다.
     * 
     * @param postId 좋아요 수를 감소시킬 포스트 ID
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, key = "#postId")
    public void decrementLikeCount(String postId) {
        log.debug("Decrementing like count for post: {}", postId);
        
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        post.decrementLikesCount();
        postRepository.save(post);
        
        log.debug("Like count decremented for post: {}, newCount={}", 
                postId, post.getLikesCount());
    }

    /**
     * 포스트의 댓글 수를 증가시킵니다.
     * Comment 서비스에서 호출되는 메서드입니다.
     * 
     * @param postId 댓글 수를 증가시킬 포스트 ID
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, key = "#postId")
    public void incrementCommentCount(String postId) {
        log.debug("Incrementing comment count for post: {}", postId);
        
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        post.incrementCommentsCount();
        postRepository.save(post);
        
        log.debug("Comment count incremented for post: {}, newCount={}", 
                postId, post.getCommentsCount());
    }

    /**
     * 포스트의 댓글 수를 감소시킵니다.
     * Comment 서비스에서 호출되는 메서드입니다.
     * 
     * @param postId 댓글 수를 감소시킬 포스트 ID
     */
    @CacheEvict(value = {"posts", "userFeed", "newsFeed", "trendingPosts"}, key = "#postId")
    public void decrementCommentCount(String postId) {
        log.debug("Decrementing comment count for post: {}", postId);
        
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        post.decrementCommentsCount();
        postRepository.save(post);
        
        log.debug("Comment count decremented for post: {}, newCount={}", 
                postId, post.getCommentsCount());
    }
}