package com.khu.acc.newsfeed.post.image;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for image upload presigned URL generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageUploadResponse {
    
    @JsonProperty("uploadUrl")
    private String uploadUrl;
    
    @JsonProperty("imageId")
    private String imageId;
    
    @JsonProperty("originalFileName")
    private String originalFileName;
    
    @JsonProperty("expectedCdnUrl")
    private String expectedCdnUrl;
    
    @JsonProperty("expiresAt")
    private String expiresAt;
    
    // Static factory method
    public static ImageUploadResponse of(String uploadUrl, String imageId, String originalFileName, String expectedCdnUrl, String expiresAt) {
        return ImageUploadResponse.builder()
                .uploadUrl(uploadUrl)
                .imageId(imageId)
                .originalFileName(originalFileName)
                .expectedCdnUrl(expectedCdnUrl)
                .expiresAt(expiresAt)
                .build();
    }
}