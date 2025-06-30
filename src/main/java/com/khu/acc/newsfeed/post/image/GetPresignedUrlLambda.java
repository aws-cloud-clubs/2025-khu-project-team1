package com.khu.acc.newsfeed.post.image;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Lambda function to generate presigned URLs for secure image uploads to S3
 */
@Slf4j
public class GetPresignedUrlLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Presigner presigner = S3Presigner.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();
    
    private final String bucketName = System.getenv("S3_BUCKET_NAME");
    private final String cloudFrontDomain = System.getenv("CLOUDFRONT_DOMAIN");
    
    // Maximum file size: 10MB
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    // Presigned URL expiration: 15 minutes
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(15);
    
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        log.info("Processing presigned URL request");
        
        try {
            // Parse request body
            String requestBody = input.getBody();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                return createErrorResponse(400, "Request body is required");
            }
            
            ImageUploadRequest uploadRequest = objectMapper.readValue(requestBody, ImageUploadRequest.class);
            
            // Validate request
            ValidationResult validation = validateRequest(uploadRequest);
            if (!validation.isValid()) {
                return createErrorResponse(400, validation.getErrorMessage());
            }
            
            // Generate unique file name
            String fileExtension = getFileExtension(uploadRequest.getFileName());
            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            String s3Key = "originals/" + uniqueFileName;
            
            // Create presigned URL
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(uploadRequest.getContentType())
                    .contentLength(uploadRequest.getFileSize())
                    .metadata(Map.of(
                            "original-filename", uploadRequest.getFileName(),
                            "uploaded-by", extractUserIdFromRequest(input),
                            "upload-timestamp", Instant.now().toString()
                    ))
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(PRESIGN_DURATION)
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(presignRequest);
            
            // Create response
            ImageUploadResponse response = ImageUploadResponse.builder()
                    .uploadUrl(presignedRequest.url().toString())
                    .imageId(uniqueFileName.substring(0, uniqueFileName.lastIndexOf('.')))
                    .originalFileName(uploadRequest.getFileName())
                    .expectedCdnUrl(String.format("https://%s/%s", cloudFrontDomain, uniqueFileName))
                    .expiresAt(Instant.now().plus(PRESIGN_DURATION).toString())
                    .build();
            
            log.info("Generated presigned URL for file: {} -> {}", uploadRequest.getFileName(), uniqueFileName);
            
            return createSuccessResponse(response);
            
        } catch (Exception e) {
            log.error("Error generating presigned URL", e);
            return createErrorResponse(500, "Internal server error: " + e.getMessage());
        }
    }
    
    private ValidationResult validateRequest(ImageUploadRequest request) {
        if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
            return ValidationResult.invalid("File name is required");
        }
        
        if (request.getContentType() == null || !isValidImageContentType(request.getContentType())) {
            return ValidationResult.invalid("Invalid content type. Only image files are allowed (JPEG, PNG, WebP)");
        }
        
        if (request.getFileSize() == null || request.getFileSize() <= 0) {
            return ValidationResult.invalid("Valid file size is required");
        }
        
        if (request.getFileSize() > MAX_FILE_SIZE) {
            return ValidationResult.invalid("File size exceeds maximum limit of 10MB");
        }
        
        String extension = getFileExtension(request.getFileName());
        if (!isValidImageExtension(extension)) {
            return ValidationResult.invalid("Invalid file extension. Only jpg, jpeg, png, webp are allowed");
        }
        
        return ValidationResult.valid();
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
    
    private String extractUserIdFromRequest(APIGatewayProxyRequestEvent input) {
        // 1. First check RequestContext authorizer (when using Lambda authorizer)
        APIGatewayProxyRequestEvent.ProxyRequestContext context = input.getRequestContext();
        if (context != null && context.getAuthorizer() != null) {
            Map<String, Object> authorizer = context.getAuthorizer();
            if (authorizer.containsKey("userId")) {
                return (String) authorizer.get("userId");
            }
            // Also check for username in case userId is not set
            if (authorizer.containsKey("username")) {
                return (String) authorizer.get("username");
            }
        }
        
        // 2. Extract from JWT token in Authorization header (fallback)
        Map<String, String> headers = input.getHeaders();
        if (headers != null && headers.containsKey("Authorization")) {
            String authHeader = headers.get("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = extractUserIdFromJwt(token);
                    if (userId != null) {
                        return userId;
                    }
                } catch (Exception e) {
                    log.warn("Failed to extract user ID from JWT token: {}", e.getMessage());
                }
            }
        }
        
        // 3. Check X-User-Id header (for development/testing)
        if (headers != null && headers.containsKey("X-User-Id")) {
            return headers.get("X-User-Id");
        }
        
        return "anonymous";
    }
    
    private String extractUserIdFromJwt(String token) {
        String jwtSecret = System.getenv("JWT_SECRET");
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            log.error("JWT_SECRET environment variable is not set");
            return null;
        }
        
        Claims claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        // JWT의 subject가 username/userId
        return claims.getSubject();
    }
    
    private APIGatewayProxyResponseEvent createSuccessResponse(Object data) {
        try {
            Map<String, Object> response = Map.of(
                    "success", true,
                    "data", data
            );
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(200)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*",
                            "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                            "Access-Control-Allow-Methods", "POST,OPTIONS"
                    ))
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Error creating success response", e);
            return createErrorResponse(500, "Error creating response");
        }
    }
    
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        try {
            Map<String, Object> response = Map.of(
                    "success", false,
                    "error", Map.of(
                            "message", message,
                            "timestamp", Instant.now().toString()
                    )
            );
            
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of(
                            "Content-Type", "application/json",
                            "Access-Control-Allow-Origin", "*",
                            "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token",
                            "Access-Control-Allow-Methods", "POST,OPTIONS"
                    ))
                    .withBody(objectMapper.writeValueAsString(response));
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("{\"success\":false,\"error\":{\"message\":\"Internal server error\"}}");
        }
    }
    
    // Helper classes for validation and response
    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}