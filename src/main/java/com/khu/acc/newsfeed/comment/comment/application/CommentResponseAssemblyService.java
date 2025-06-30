package com.khu.acc.newsfeed.comment.comment.application;

import com.khu.acc.newsfeed.comment.comment.domain.Comment;
import com.khu.acc.newsfeed.comment.comment.domain.CommentRepository;
import com.khu.acc.newsfeed.comment.comment.interfaces.dto.CommentResponse;
import com.khu.acc.newsfeed.user.interfaces.dto.UserResponse;
import com.khu.acc.newsfeed.user.domain.User;
import com.khu.acc.newsfeed.user.application.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 댓글 응답 조립을 담당하는 서비스
 * N+1 쿼리 문제 해결 및 사용자 데이터 배치 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentResponseAssemblyService {
    
    private final UserService userService;
    private final CommentRepository commentRepository;
    
    private static final int DEFAULT_REPLY_LIMIT = 5;
    
    /**
     * 댓글 리스트를 CommentResponse로 변환 (사용자 정보 포함)
     */
    public List<CommentResponse> assembleCommentResponses(List<Comment> comments, boolean includeReplies) {
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 1. 모든 사용자 ID 수집
        Set<String> userIds = comments.stream()
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
        
        // 2. 대댓글이 포함된 경우 대댓글 작성자 ID도 수집
        if (includeReplies) {
            Set<String> replyUserIds = comments.stream()
                    .map(Comment::getCommentId)
                    .map(this::getReplyUserIds)
                    .flatMap(Set::stream)
                    .collect(Collectors.toSet());
            userIds.addAll(replyUserIds);
        }
        
        // 3. 배치로 사용자 정보 조회
        Map<String, User> userMap = userService.findUsersByIds(userIds);
        
        // 4. CommentResponse 조립
        return comments.stream()
                .map(comment -> assembleCommentResponse(comment, userMap, includeReplies))
                .collect(Collectors.toList());
    }
    
    /**
     * 단일 댓글을 CommentResponse로 변환
     */
    public CommentResponse assembleCommentResponse(Comment comment, boolean includeReplies) {
        Set<String> userIds = new HashSet<>();
        userIds.add(comment.getUserId());
        
        if (includeReplies) {
            userIds.addAll(getReplyUserIds(comment.getCommentId()));
        }
        
        Map<String, User> userMap = userService.findUsersByIds(userIds);
        return assembleCommentResponse(comment, userMap, includeReplies);
    }
    
    /**
     * 댓글과 사용자 맵을 이용해 CommentResponse 조립
     */
    private CommentResponse assembleCommentResponse(Comment comment, Map<String, User> userMap, boolean includeReplies) {
        CommentResponse response = CommentResponse.from(comment);
        
        // 작성자 정보 설정
        User author = userMap.get(comment.getUserId());
        if (author != null) {
            response.setAuthor(UserResponse.from(author));
        }
        
        // 대댓글 정보 포함
        if (includeReplies && !comment.isReply()) {
            attachReplyInfo(response, comment.getCommentId(), userMap);
        }
        
        return response;
    }
    
    /**
     * 대댓글 정보를 CommentResponse에 첨부
     */
    private void attachReplyInfo(CommentResponse response, String commentId, Map<String, User> userMap) {
        List<Comment> initialReplies = getInitialReplies(commentId, DEFAULT_REPLY_LIMIT);
        
        if (!initialReplies.isEmpty()) {
            List<CommentResponse> replyResponses = initialReplies.stream()
                    .map(reply -> {
                        CommentResponse replyResponse = CommentResponse.from(reply);
                        User replyAuthor = userMap.get(reply.getUserId());
                        if (replyAuthor != null) {
                            replyResponse.setAuthor(UserResponse.from(replyAuthor));
                        }
                        return replyResponse;
                    })
                    .collect(Collectors.toList());
            
            response.setReplies(replyResponses);
            
            // 총 대댓글 수 및 더보기 여부
            Long totalReplyCount = commentRepository.countByParentCommentIdAndIsActiveTrue(commentId);
            response.setTotalReplyCount(totalReplyCount);
            response.setHasMoreReplies(totalReplyCount > DEFAULT_REPLY_LIMIT);
        }
    }
    
    /**
     * 댓글의 초기 대댓글 목록 조회
     */
    private List<Comment> getInitialReplies(String commentId, int limit) {
        List<Comment> allReplies = commentRepository
                .findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(commentId);
        return allReplies.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 댓글의 대댓글 작성자 ID들 수집
     */
    private Set<String> getReplyUserIds(String commentId) {
        return commentRepository
                .findByParentCommentIdAndIsActiveTrueOrderByCreatedAtAsc(commentId)
                .stream()
                .limit(DEFAULT_REPLY_LIMIT)
                .map(Comment::getUserId)
                .collect(Collectors.toSet());
    }
}