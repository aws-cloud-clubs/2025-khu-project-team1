package com.khu.acc.newsfeed.comment.comment.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentUpdateRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    private String content;
    
    // Static factory method
    public static CommentUpdateRequest of(String content) {
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.content = content;
        return request;
    }
}