package com.khu.acc.newsfeed.post.post.interfaces.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;

    private Set<String> tags;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    // Static factory methods
    public static PostCreateRequest of(String content, List<String> imageUrls, Set<String> tags, String location) {
        PostCreateRequest request = new PostCreateRequest();
        request.content = content;
        request.imageUrls = imageUrls;
        request.tags = tags;
        request.location = location;
        return request;
    }

    public static PostCreateRequest withContent(String content) {
        return of(content, null, null, null);
    }

    public static PostCreateRequest withContentAndImages(String content, List<String> imageUrls) {
        return of(content, imageUrls, null, null);
    }

    public static PostCreateRequest withContentAndTags(String content, Set<String> tags) {
        return of(content, null, tags, null);
    }
}