package com.khu.acc.newsfeed.post.post.application.command;

import com.khu.acc.newsfeed.post.post.interfaces.dto.PostCreateRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

/**
 * 포스트 생성을 위한 서비스 레이어 Command DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateCommand {
    
    private String userId;
    private String content;
    private List<String> imageUrls;
    private Set<String> tags;
    private String location;
    
    /**
     * PostCreateRequest와 userId로부터 PostCreateCommand를 생성
     */
    public static PostCreateCommand from(PostCreateRequest request, String userId) {
        return PostCreateCommand.builder()
                .userId(userId)
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .tags(request.getTags())
                .location(request.getLocation())
                .build();
    }
    
    /**
     * 기본 포스트 생성을 위한 팩토리 메서드
     */
    public static PostCreateCommand of(String userId, String content) {
        return PostCreateCommand.builder()
                .userId(userId)
                .content(content)
                .build();
    }
    
    /**
     * 이미지가 포함된 포스트 생성을 위한 팩토리 메서드
     */
    public static PostCreateCommand withImages(String userId, String content, List<String> imageUrls) {
        return PostCreateCommand.builder()
                .userId(userId)
                .content(content)
                .imageUrls(imageUrls)
                .build();
    }
    
    /**
     * 태그가 포함된 포스트 생성을 위한 팩토리 메서드
     */
    public static PostCreateCommand withTags(String userId, String content, Set<String> tags) {
        return PostCreateCommand.builder()
                .userId(userId)
                .content(content)
                .tags(tags)
                .build();
    }
}