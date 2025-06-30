package com.khu.acc.newsfeed.post.image;

import com.khu.acc.newsfeed.common.exception.image.ImageErrorCode;
import com.khu.acc.newsfeed.common.exception.image.ImageUploadException;
import com.khu.acc.newsfeed.common.exception.image.ImageValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 이미지 업로드 관련 서비스
 * S3 presigned URL 생성 및 이미지 URL 검증
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUploadService {

    @Value("${aws.s3.bucket-name:}")
    private String bucketName;
    
    @Value("${aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    private final S3Presigner presigner = S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();

    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    // Presigned URL expiration: 15 minutes
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);

    /**
     * 이미지 업로드를 위한 presigned URL 생성
     */
    public ImageUploadResponse generateUploadUrl(ImageUploadRequest request, String userId) {
        // 요청 검증
        validateRequest(request);

        // 고유 파일명 생성
        String fileExtension = getFileExtension(request.getFileName());
        String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
        String s3Key = "originals/" + uniqueFileName;

        // Presigned URL 생성
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(getBucketName())
                .key(s3Key)
                .contentType(request.getContentType())
                .contentLength(request.getFileSize())
                .metadata(Map.of(
                        "original-filename", request.getFileName(),
                        "uploaded-by", userId,
                        "upload-timestamp", Instant.now().toString()
                ))
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .putObjectRequest(putObjectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest;
        try {
            presignedRequest = presigner.presignPutObject(presignRequest);
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for user {}: {}", userId, e.getMessage());
            throw new ImageUploadException(ImageErrorCode.PRESIGNED_URL_GENERATION_FAILED, e);
        }

        // 응답 생성
        ImageUploadResponse response = ImageUploadResponse.builder()
                .uploadUrl(presignedRequest.url().toString())
                .imageId(uniqueFileName.substring(0, uniqueFileName.lastIndexOf('.')))
                .originalFileName(request.getFileName())
                .expectedCdnUrl(String.format("https://%s/%s", getCloudFrontDomain(), uniqueFileName))
                .expiresAt(Instant.now().plus(PRESIGN_DURATION).toString())
                .build();

        log.info("Generated presigned URL for user {}: {} -> {}", userId, request.getFileName(), uniqueFileName);
        
        return response;
    }

    /**
     * 이미지 URL이 유효한지 검증
     */
    public boolean validateImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return false;
        }

        String cloudFrontDomain = getCloudFrontDomain();
        if (cloudFrontDomain.isEmpty()) {
            log.warn("CloudFront domain not configured, skipping URL validation");
            return true;
        }

        // CDN 도메인으로 시작하는지 확인
        if (!imageUrl.startsWith("https://" + cloudFrontDomain + "/")) {
            log.debug("Image URL does not match CDN domain: {}", imageUrl);
            return false;
        }

        // 파일 확장자 검증
        String extension = getFileExtensionFromUrl(imageUrl);
        if (!isValidImageExtension(extension)) {
            log.debug("Invalid image extension in URL: {}", imageUrl);
            return false;
        }

        return true;
    }

    /**
     * 원본 이미지 URL에서 최적화된 이미지 URL들 생성
     */
    public List<String> getOptimizedImageUrls(String originalUrl) {
        if (!validateImageUrl(originalUrl)) {
            return List.of(originalUrl);
        }

        String baseUrl = originalUrl.substring(0, originalUrl.lastIndexOf('.'));
        String extension = getFileExtensionFromUrl(originalUrl);
        
        return List.of(
                baseUrl + "_thumbnail." + extension,
                baseUrl + "_small." + extension,
                baseUrl + "_medium." + extension,
                baseUrl + "_large." + extension,
                baseUrl + "_original." + extension
        );
    }

    /**
     * 이미지 URL 목록 검증
     */
    public boolean validateImageUrls(List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return true; // 빈 목록은 유효
        }

        return imageUrls.stream().allMatch(this::validateImageUrl);
    }

    // Private helper methods

    private void validateRequest(ImageUploadRequest request) {
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            throw new ImageValidationException(ImageErrorCode.INVALID_FILE_NAME);
        }

        if (request.getContentType() == null || !isValidImageContentType(request.getContentType())) {
            throw new ImageValidationException(ImageErrorCode.INVALID_CONTENT_TYPE);
        }

        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            throw new ImageValidationException(ImageErrorCode.INVALID_FILE_SIZE);
        }

        if (request.getFileSize() > MAX_FILE_SIZE) {
            throw new ImageValidationException(ImageErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = getFileExtension(request.getFileName());
        if (!isValidImageExtension(extension)) {
            throw new ImageValidationException(ImageErrorCode.INVALID_FILE_EXTENSION);
        }
    }

    private boolean isValidImageContentType(String contentType) {
        return contentType.equals("image/jpeg") || 
               contentType.equals("image/png") || 
               contentType.equals("image/webp");
    }

    private boolean isValidImageExtension(String extension) {
        return extension.equalsIgnoreCase("jpg") || 
               extension.equalsIgnoreCase("jpeg") || 
               extension.equalsIgnoreCase("png") || 
               extension.equalsIgnoreCase("webp");
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }

    private String getFileExtensionFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        return getFileExtension(fileName);
    }

    private String getBucketName() {
        if (bucketName == null || bucketName.isEmpty()) {
            // Fallback to environment variable
            bucketName = System.getenv("S3_BUCKET_NAME");
            if (bucketName == null || bucketName.isEmpty()) {
                throw new ImageUploadException(ImageErrorCode.S3_BUCKET_NOT_CONFIGURED);
            }
        }
        return bucketName;
    }

    private String getCloudFrontDomain() {
        if (cloudFrontDomain == null || cloudFrontDomain.isEmpty()) {
            // Fallback to environment variable
            cloudFrontDomain = System.getenv("CLOUDFRONT_DOMAIN");
            if (cloudFrontDomain == null) {
                cloudFrontDomain = "";
            }
        }
        return cloudFrontDomain;
    }
}