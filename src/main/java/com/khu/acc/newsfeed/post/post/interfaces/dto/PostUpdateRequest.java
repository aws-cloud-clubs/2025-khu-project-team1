package com.khu.acc.newsfeed.post.post.interfaces.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {

    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;

    private Set<String> tags;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    // Static factory methods
    public static PostUpdateRequest of(String content, Set<String> tags, String location) {
        PostUpdateRequest request = new PostUpdateRequest();
        request.content = content;
        request.tags = tags;
        request.location = location;
        return request;
    }

    public static PostUpdateRequest withContent(String content) {
        return of(content, null, null);
    }

    public static PostUpdateRequest withTags(Set<String> tags) {
        return of(null, tags, null);
    }

    public static PostUpdateRequest withLocation(String location) {
        return of(null, null, location);
    }
}
