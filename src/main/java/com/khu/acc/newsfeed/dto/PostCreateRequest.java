package com.khu.acc.newsfeed.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@Builder
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
}