package com.khu.acc.newsfeed.common.dto.sns;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SNS로 발행되는 포스트 이벤트 메시지
 * 뉴스피드 서비스와 알림 서비스에서 공통으로 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostEventMessage {
    
    private String eventType;     // "POST_CREATED", "POST_DELETED", "POST_UPDATED"
    private String postId;
    private String authorId;
    private Instant timestamp;
    
    // 포스트 상세 정보 (POST_CREATED, POST_UPDATED용)
    private String content;
    private List<String> imageUrls;
    private List<String> tags;
    private String location;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();  // 확장 가능한 추가 데이터
    
    // 정적 팩토리 메서드
    public static PostEventMessage forPostCreated(String postId, String authorId, 
                                                  String content, List<String> imageUrls, 
                                                  List<String> tags, String location) {
        return PostEventMessage.builder()
                .eventType("POST_CREATED")
                .postId(postId)
                .authorId(authorId)
                .content(content)
                .imageUrls(imageUrls)
                .tags(tags)
                .location(location)
                .timestamp(Instant.now())
                .build();
    }
    
    public static PostEventMessage forPostDeleted(String postId, String authorId) {
        return PostEventMessage.builder()
                .eventType("POST_DELETED")
                .postId(postId)
                .authorId(authorId)
                .timestamp(Instant.now())
                .build();
    }
    
    public static PostEventMessage forPostUpdated(String postId, String authorId,
                                                  String content, List<String> imageUrls,
                                                  List<String> tags, String location) {
        return PostEventMessage.builder()
                .eventType("POST_UPDATED")
                .postId(postId)
                .authorId(authorId)
                .content(content)
                .imageUrls(imageUrls)
                .tags(tags)
                .location(location)
                .timestamp(Instant.now())
                .build();
    }
    
    /**
     * SNS 메시지 속성으로 사용할 이벤트 타입 반환
     */
    public String getEventTypeForMessageAttribute() {
        return this.eventType;
    }
}