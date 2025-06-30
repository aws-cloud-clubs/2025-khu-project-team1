package com.khu.acc.newsfeed.comment.comment.application;

import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.comment.comment.domain.CommentRepository;
import com.khu.acc.newsfeed.comment.comment.interfaces.dto.CommentResponse;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.common.exception.comment.CommentAccessDeniedException;
import com.khu.acc.newsfeed.common.exception.comment.CommentNotFoundException;
import com.khu.acc.newsfeed.common.exception.comment.CommentOperationException;
import com.khu.acc.newsfeed.common.exception.user.UserNotFoundException;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.common.service.CursorPaginationService;
import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import com.khu.acc.newsfeed.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;
    private final CommentResponseAssemblyService responseAssemblyService;
    private final CursorPaginationService paginationService;
    
    private static final int MAX_COMMENT_LENGTH = 1000;
    private static final int DEFAULT_REPLY_LIMIT = 5;
    
    // 댓글 작성
    @CacheEvict(value = {"postComments", "commentCount"}, allEntries = true) 
    public Comment createComment(String userId, String postId, String content, String parentCommentId) {
        return createCommentInternal(userId, postId, content, parentCommentId);
    }
    
    private Comment createCommentInternal(String userId, String postId, String content, String parentCommentId) {
        // 입력 유효성 검사
        validateCommentContent(content);
        
        // 포스트 존재 확인
        Post post = postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 사용자 정보 조회
        User user = userService.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // 대댓글인 경우 부모 댓글 확인
        if (parentCommentId != null && !parentCommentId.isEmpty()) {
            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> CommentNotFoundException.parentNotFound(parentCommentId));
            
            // 대댓글의 대댓글 방지 (깊이 제한)
            if (parentComment.isReply()) {
                throw CommentOperationException.replyDepthExceeded();
            }
            
            // 삭제된 댓글에 대한 대댓글 방지
            if (parentComment.getIsActive()) {
                throw CommentNotFoundException.deleted(parentCommentId);
            }
        }
        
        try {
            // 댓글 생성
            Comment comment = parentCommentId != null ?
                    Comment.replyOf(postId, userId, content, parentCommentId, user.getDisplayName(), user.getProfileImageUrl()) :
                    Comment.of(postId, userId, content, user.getDisplayName(), user.getProfileImageUrl());
            
            Comment savedComment = commentRepository.save(comment);
            
            // 포스트의 댓글 수 증가
            post.incrementCommentsCount();
            postRepository.save(post);
            
            log.info("Comment created: {} by user: {} on post: {}", savedComment.getCommentId(), userId, postId);
            return savedComment;
            
        } catch (Exception e) {
            log.error("Failed to create comment for post {} by user {}", postId, userId, e);
            throw CommentOperationException.creationFailed();
        }
    }
    
    // 댓글 수정
    @CacheEvict(value = {"postComments", "commentDetails"}, allEntries = true)
    public Comment updateComment(String commentId, String userId, String newContent) {
        return updateCommentInternal(commentId, userId, newContent);
    }
    
    private Comment updateCommentInternal(String commentId, String userId, String newContent) {
        // 입력 유효성 검사
        validateCommentContent(newContent);
        
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));
        
        // 삭제된 댓글 수정 방지
        if (comment.getIsActive()) {
            throw CommentOperationException.alreadyDeleted();
        }
        
        // 권한 확인
        if (!comment.getUserId().equals(userId)) {
            throw CommentAccessDeniedException.updateDenied();
        }
        
        try {
            // 댓글 수정
            comment.updateContent(newContent);
            Comment updatedComment = commentRepository.save(comment);
            
            log.info("Comment updated: {} by user: {}", commentId, userId);
            return updatedComment;
            
        } catch (Exception e) {
            log.error("Failed to update comment {}", commentId, e);
            throw CommentOperationException.updateFailed();
        }
    }
    
    // 댓글 삭제 (소프트 삭제)
    @CacheEvict(value = {"postComments", "commentDetails", "commentCount"}, allEntries = true)
    public void deleteComment(String commentId, String userId) {
        // 댓글 조회
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));
        
        // 이미 삭제된 댓글 확인
        if (comment.getIsActive()) {
            throw CommentOperationException.alreadyDeleted();
        }
        
        // 권한 확인
        if (!comment.getUserId().equals(userId)) {
            throw CommentAccessDeniedException.deleteDenied();
        }
        
        try {
            // 소프트 삭제
            comment.markAsDeleted();
            commentRepository.save(comment);
            
            // 대댓글이 없는 경우에만 포스트 댓글 수 감소
            Long replyCount = commentRepository.countByParentCommentIdAndIsActiveTrue(commentId);
            if (replyCount == 0) {
                Post post = postRepository.findByPostId(comment.getPostId())
                        .orElseThrow(() -> new PostNotFoundException(comment.getPostId()));
                post.decrementCommentsCount();
                postRepository.save(post);
            }
            
            log.info("Comment soft deleted: {} by user: {}", commentId, userId);
            
        } catch (Exception e) {
            log.error("Failed to delete comment {}", commentId, e);
            throw CommentOperationException.deletionFailed();
        }
    }
    
    // 댓글 조회
    @Cacheable(value = "commentDetails", key = "#commentId")
    public Optional<Comment> findById(String commentId) {
        return commentRepository.findById(commentId);
    }
    
    // 포스트의 댓글 목록 조회 (스크롤 페이징)
    @Cacheable(value = "postComments", key = "#postId + '-' + #scrollRequest.cursor + '-' + #scrollRequest.limit")
    public ScrollResponse<CommentResponse> getPostComments(String postId, ScrollRequest scrollRequest, String currentUserId) {
        // 포스트 존재 확인
        postRepository.findByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        
        // 모든 최상위 댓글 조회
        List<Comment> topLevelComments = commentRepository
                .findByPostIdAndParentCommentIdIsNullAndIsActiveTrueOrderByCreatedAtAsc(postId);
        
        // 스크롤 페이징 처리
        ScrollResponse<Comment> scrollResponse = paginationService.paginate(
                topLevelComments, scrollRequest, Comment::getCommentId);
        
        // CommentResponse 조립 (사용자 정보 및 대댓글 포함)
        List<CommentResponse> responses = responseAssemblyService
                .assembleCommentResponses(scrollResponse.getContent(), true);
        
        return ScrollResponse.of(
                responses,
                scrollResponse.getNextCursor(),
                scrollResponse.getPreviousCursor(),
                scrollResponse.isHasNext(),
                scrollResponse.isHasPrevious()
        );
    }
    
    // 댓글의 대댓글 조회 (더보기 방식)
    public List<CommentResponse> getCommentReplies(String commentId, int offset, int limit) {
        // 부모 댓글 존재 확인
        commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));
        
        // 대댓글 조회
        List<Comment> replies = commentRepository
                .findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(commentId);
        
        // 페이지네이션 처리
        int startIndex = Math.min(offset, replies.size());
        int endIndex = Math.min(startIndex + limit, replies.size());
        List<Comment> paginatedReplies = replies.subList(startIndex, endIndex);
        
        // CommentResponse 조립
        return responseAssemblyService.assembleCommentResponses(paginatedReplies, false);
    }
    
    // 사용자의 댓글 목록 조회
    @Cacheable(value = "userComments", key = "#userId + '-' + #scrollRequest.cursor + '-' + #scrollRequest.limit")
    public ScrollResponse<CommentResponse> getUserComments(String userId, ScrollRequest scrollRequest) {
        List<Comment> userComments = commentRepository
                .findByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId);
        
        ScrollResponse<Comment> scrollResponse = paginationService.paginate(
                userComments, scrollRequest, Comment::getCommentId);
        
        // CommentResponse 조립
        List<CommentResponse> responses = responseAssemblyService
                .assembleCommentResponses(scrollResponse.getContent(), false);
        
        return ScrollResponse.of(
                responses,
                scrollResponse.getNextCursor(),
                scrollResponse.getPreviousCursor(),
                scrollResponse.isHasNext(),
                scrollResponse.isHasPrevious()
        );
    }
    
    // 포스트의 댓글 수 조회
    @Cacheable(value = "commentCount", key = "#postId")
    public Long getCommentCount(String postId) {
        return commentRepository.countByPostIdAndIsActiveTrue(postId);
    }
    
    // Helper methods
    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw CommentOperationException.contentEmpty();
        }
        if (content.length() > MAX_COMMENT_LENGTH) {
            throw CommentOperationException.contentTooLong();
        }
    }
}
