package com.khu.acc.newsfeed.comment.commentlike.application;

import com.khu.acc.newsfeed.comment.commentlike.domain.CommentLike;
import com.khu.acc.newsfeed.comment.commentlike.domain.CommentLikeRepository;
import com.khu.acc.newsfeed.comment.commentlike.messaging.event.CommentLikeCreatedEvent;
import com.khu.acc.newsfeed.comment.commentlike.messaging.event.CommentLikeDeletedEvent;
import com.khu.acc.newsfeed.common.dto.ScrollRequest;
import com.khu.acc.newsfeed.common.dto.ScrollResponse;
import com.khu.acc.newsfeed.common.exception.comment.CommentNotFoundException;
import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.comment.comment.domain.CommentRepository;
import com.khu.acc.newsfeed.common.service.CursorPaginationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class CommentLikeService {

    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final CursorPaginationService paginationService;

    public CommentLike likeComment(String commentId, String userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));

        // 삭제된 댓글 좋아요 방지
        if (comment.getIsActive()) {
            throw CommentNotFoundException.deleted(commentId);
        }

        // Idempotent 처리 - 이미 좋아요가 있으면 기존 것 반환
        CommentLike existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId).orElse(null);
        if (existingLike != null) {
            log.debug("User {} already liked comment {}, returning existing like", userId, commentId);
            return existingLike;
        }

        // 새 좋아요 생성
        CommentLike newLike = CommentLike.of(commentId, userId);
        CommentLike savedLike = commentLikeRepository.save(newLike);

        log.info("User {} liked comment {}", userId, commentId);
        return savedLike;
    }

    public void unlikeComment(String commentId, String userId) {
        CommentLike existingLike = commentLikeRepository.findByCommentIdAndUserId(commentId, userId).orElse(null);
        if (existingLike == null) {
            log.debug("User {} has not liked comment {}, nothing to remove", userId, commentId);
            return;
        }

        // 좋아요 삭제
        commentLikeRepository.deleteByCommentIdAndUserId(commentId, userId);

        log.info("User {} unliked comment {}", userId, commentId);
    }

    public ScrollResponse<CommentLike> getCommentLikes(String commentId, ScrollRequest scrollRequest) {
        // 댓글 존재 확인
        commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));

        List<CommentLike> allLikes = commentLikeRepository.findByCommentIdOrderByCreatedAtDesc(commentId);
        return paginationService.paginate(allLikes, scrollRequest, CommentLike::getCommentLikeId);
    }

    public ScrollResponse<CommentLike> getUserCommentLikes(String userId, ScrollRequest scrollRequest) {
        List<CommentLike> allLikes = commentLikeRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return paginationService.paginate(allLikes, scrollRequest, CommentLike::getCommentLikeId);
    }

    public boolean hasUserLikedComment(String commentId, String userId) {
        return commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
    }

    public Long getCommentLikesCount(String commentId) {
        // 댓글에서 직접 카운트 조회 (비동기 집계 반영된 값)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> CommentNotFoundException.notFound(commentId));
        
        return comment.getLikesCount();
    }

    public Long getUserCommentLikesCount(String userId) {
        return commentLikeRepository.countByUserId(userId);
    }

    // 댓글 삭제 시 관련 좋아요 모두 삭제
    public void deleteAllLikesForComment(String commentId) {
        commentLikeRepository.deleteByCommentId(commentId);
        log.info("Deleted all likes for comment {}", commentId);
    }

    // 배치 좋아요 상태 확인 (여러 댓글에 대한 사용자의 좋아요 상태)
    public Set<String> getLikedCommentIds(String userId, Set<String> commentIds) {
        return commentIds.stream()
                .filter(commentId -> commentLikeRepository.existsByCommentIdAndUserId(commentId, userId))
                .collect(Collectors.toSet());
    }

    // 최근 댓글 좋아요 활동 조회
    public List<CommentLike> getRecentCommentLikes(Instant since) {
        return commentLikeRepository.findByCreatedAtAfterOrderByCreatedAtDesc(since);
    }

    // 특정 댓글의 최근 좋아요들
    public List<CommentLike> getRecentCommentLikes(String commentId, Instant since) {
        return commentLikeRepository.findByCommentIdAndCreatedAtAfterOrderByCreatedAtDesc(commentId, since);
    }

}