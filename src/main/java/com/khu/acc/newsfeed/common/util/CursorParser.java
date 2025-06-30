package com.khu.acc.newsfeed.common.util;

import com.khu.acc.newsfeed.common.exception.application.post.PostNotFoundException;
import com.khu.acc.newsfeed.post.post.domain.Post;
import com.khu.acc.newsfeed.post.post.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 커서 파싱 유틸리티
 * 다양한 커서 형식을 처리하고 Instant 타입으로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CursorParser {

    private final PostRepository postRepository;

    /**
     * 커서를 Instant로 파싱
     * 지원 형식:
     * 1. ISO 8601 시간 문자열 (예: "2023-12-25T10:15:30Z")
     * 2. Post ID (해당 포스트의 생성 시간으로 변환)
     * 3. 복합 커서 "timestamp#id" (timestamp 부분 사용)
     */
    public Instant parseCursorToInstant(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return Instant.now();
        }

        try {
            // 복합 커서인 경우 (timestamp#id)
            if (cursor.contains("#")) {
                String[] parts = cursor.split("#");
                return Instant.parse(parts[0]);
            }

            // 단순 시간 형식인 경우
            return Instant.parse(cursor);
        } catch (Exception e) {
            // 커서가 ID 형식인 경우, 해당 포스트의 생성 시간을 조회
            try {
                Post post = postRepository.findByPostId(cursor)
                        .orElseThrow(() -> new PostNotFoundException(cursor));
                return post.getCreatedAt();
            } catch (Exception ex) {
                log.warn("Failed to parse cursor: {}, using current time", cursor);
                return Instant.now();
            }
        }
    }

    /**
     * 커서에서 ID 추출 (복합 커서 지원)
     */
    public String extractIdFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        
        // 복합 커서인 경우 (sortKey#id)
        String[] parts = cursor.split("#");
        return parts.length > 1 ? parts[1] : cursor;
    }

    /**
     * 커서에서 정렬 키 추출
     */
    public String extractSortKeyFromCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        
        String[] parts = cursor.split("#");
        return parts[0];
    }

    /**
     * 복합 커서 생성
     */
    public String createCompositeCursor(String sortKey, String id) {
        return sortKey + "#" + id;
    }

    /**
     * 시간 기반 커서 생성
     */
    public String createTimeCursor(Instant timestamp) {
        return timestamp.toString();
    }

    /**
     * 포스트 기반 복합 커서 생성
     */
    public String createPostCursor(Post post) {
        return createCompositeCursor(post.getCreatedAt().toString(), post.getPostId());
    }

    /**
     * 좋아요 수 기반 복합 커서 생성
     */
    public String createLikesCursor(Post post) {
        return createCompositeCursor(post.getLikesCount().toString(), post.getPostId());
    }
}