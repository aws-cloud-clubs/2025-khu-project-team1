package com.khu.acc.newsfeed.post.post.application;

import com.khu.acc.newsfeed.post.post.application.command.PostCreateCommand;
import com.khu.acc.newsfeed.post.post.application.command.PostUpdateCommand;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.post.post.domain.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 포스트 파사드 서비스
 * PostController에서 사용하는 통합 서비스로, 
 * PostCommandService와 PostQueryService를 조합하여 
 * 완전한 포스트 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    // === Command Operations (위임) ===

    /**
     * 새로운 포스트를 생성합니다.
     */
    public Post createPost(PostCreateCommand command) {
        log.debug("Creating post via facade: userId={}", command.getUserId());
        return postCommandService.createPost(command);
    }

    /**
     * 포스트를 수정합니다.
     */
    public Post updatePost(PostUpdateCommand command) {
        log.debug("Updating post via facade: postId={}", command.getPostId());
        return postCommandService.updatePost(command);
    }

    /**
     * 포스트를 삭제합니다.
     */
    public void deletePost(String postId, String userId) {
        log.debug("Deleting post via facade: postId={}", postId);
        postCommandService.deletePost(postId, userId);
    }

    // === Query Operations (위임) ===

    /**
     * 포스트 ID로 단일 포스트를 조회합니다.
     */
    public Optional<Post> findById(String postId) {
        log.debug("Finding post by id via facade: postId={}", postId);
        return postQueryService.findById(postId);
    }

    /**
     * 사용자의 포스트를 페이징하여 조회합니다.
     */
    public ScrollResponse<Post> findUserPosts(String userId, ScrollRequest scrollRequest) {
        log.debug("Finding user posts via facade: userId={}", userId);
        return postQueryService.findUserPostsWithPaging(userId, scrollRequest);
    }

    /**
     * 인기 포스트(트렌딩)를 페이징하여 조회합니다.
     */
    public ScrollResponse<Post> findTrendingPosts(ScrollRequest scrollRequest) {
        log.debug("Finding trending posts via facade");
        return postQueryService.findTrendingPostsWithPaging(scrollRequest);
    }

    /**
     * 태그별 포스트를 페이징하여 조회합니다.
     */
    public ScrollResponse<Post> findPostsByTag(String tag, ScrollRequest scrollRequest) {
        log.debug("Finding posts by tag via facade: tag={}", tag);
        return postQueryService.findPostsByTagWithPaging(tag, scrollRequest);
    }

    /**
     * 내용으로 포스트를 검색합니다.
     */
    public ScrollResponse<Post> searchPosts(String content, ScrollRequest scrollRequest) {
        log.debug("Searching posts via facade: content={}", content);
        return postQueryService.searchPostsWithPaging(content, scrollRequest);
    }

    // === Additional Utility Methods ===

    /**
     * 포스트 존재 여부를 확인합니다.
     */
    public boolean existsById(String postId) {
        log.debug("Checking post existence via facade: postId={}", postId);
        return postQueryService.existsById(postId);
    }

    /**
     * 사용자의 활성 포스트 수를 조회합니다.
     */
    public long countUserPosts(String userId) {
        log.debug("Counting user posts via facade: userId={}", userId);
        return postQueryService.countUserPosts(userId);
    }

    /**
     * 최근 포스트 수를 조회합니다.
     */
    public long countRecentPosts(String userId, int sinceHours) {
        log.debug("Counting recent posts via facade: userId={}, sinceHours={}", userId, sinceHours);
        return postQueryService.countRecentPostsByHours(userId, sinceHours);
    }

}
