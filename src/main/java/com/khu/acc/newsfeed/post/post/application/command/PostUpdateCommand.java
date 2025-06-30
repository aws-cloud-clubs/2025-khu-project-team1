package com.khu.acc.newsfeed.post.post.application.command;

import com.khu.acc.newsfeed.post.post.interfaces.dto.PostUpdateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * 포스트 수정을 위한 서비스 레이어 Command DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateCommand {
    
    private String postId;
    private String userId;
    private String content;
    private Set<String> tags;
    private String location;
    
    /**
     * PostUpdateRequest와 postId, userId로부터 PostUpdateCommand를 생성
     */
    public static PostUpdateCommand from(PostUpdateRequest request, String postId, String userId) {
        return PostUpdateCommand.builder()
                .postId(postId)
                .userId(userId)
                .content(request.getContent())
                .tags(request.getTags())
                .location(request.getLocation())
                .build();
    }
    
    /**
     * 내용만 수정하는 팩토리 메서드
     */
    public static PostUpdateCommand withContent(String postId, String userId, String content) {
        return PostUpdateCommand.builder()
                .postId(postId)
                .userId(userId)
                .content(content)
                .build();
    }
    
    /**
     * 태그만 수정하는 팩토리 메서드
     */
    public static PostUpdateCommand withTags(String postId, String userId, Set<String> tags) {
        return PostUpdateCommand.builder()
                .postId(postId)
                .userId(userId)
                .tags(tags)
                .build();
    }
    
    /**
     * 위치만 수정하는 팩토리 메서드
     */
    public static PostUpdateCommand withLocation(String postId, String userId, String location) {
        return PostUpdateCommand.builder()
                .postId(postId)
                .userId(userId)
                .location(location)
                .build();
    }
    
    /**
     * 수정할 필드가 있는지 확인
     */
    public boolean hasUpdates() {
        return content != null || tags != null || location != null;
    }
}