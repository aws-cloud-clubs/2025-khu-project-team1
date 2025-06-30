package com.khu.acc.newsfeed.post.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for image upload presigned URL generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadRequest {
    
    @NotBlank(message = "File name is required")
    @JsonProperty("fileName")
    private String fileName;
    
    @NotBlank(message = "Content type is required")
    @JsonProperty("contentType")
    private String contentType;
    
    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("description")
    private String description;
    
    // Static factory method
    public static ImageUploadRequest of(String fileName, String contentType, Long fileSize) {
        return ImageUploadRequest.builder()
                .fileName(fileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .build();
    }
    
    public static ImageUploadRequest withDescription(String fileName, String contentType, Long fileSize, String description) {
        return ImageUploadRequest.builder()
                .fileName(fileName)
                .contentType(contentType)
                .fileSize(fileSize)
                .description(description)
                .build();
    }
}