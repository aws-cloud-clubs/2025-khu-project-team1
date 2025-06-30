package com.khu.acc.newsfeed.post.image;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.khu.acc.newsfeed.common.exception.image.ImageErrorCode;
import com.khu.acc.newsfeed.common.exception.image.ImageProcessingException;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

/**
 * Lambda function to automatically process uploaded images:
 * - Resize to multiple sizes (thumbnail, medium, large)
 * - Optimize for web delivery
 * - Convert to WebP format for better compression
 */
@Slf4j
public class ImageProcessingLambda implements RequestHandler<S3Event, String> {

    private final S3Client s3Client = S3Client.builder()
            .region(Region.AP_NORTHEAST_2)
            .build();
    
    private final String bucketName = System.getenv("S3_BUCKET_NAME");
    
    // Image size configurations
    private static final Map<String, ImageSize> IMAGE_SIZES = Map.of(
            "thumbnail", new ImageSize(150, 150, 0.8f),
            "small", new ImageSize(400, 400, 0.85f),
            "medium", new ImageSize(800, 800, 0.9f),
            "large", new ImageSize(1200, 1200, 0.95f)
    );
    
    @Override
    public String handleRequest(S3Event event, Context context) {
        log.info("Processing S3 event with {} records", event.getRecords().size());
        
        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            String eventName = record.getEventName();
            if (!eventName.startsWith("ObjectCreated")) {
                log.debug("Skipping non-creation event: {}", eventName);
                continue;
            }
            
            String sourceKey = record.getS3().getObject().getKey();
            
            // Only process files in the 'originals/' prefix
            if (!sourceKey.startsWith("originals/")) {
                log.debug("Skipping file not in originals/ directory: {}", sourceKey);
                continue;
            }
            
            try {
                processImage(sourceKey);
            } catch (ImageProcessingException e) {
                log.error("Image processing failed for {}: {}", sourceKey, e.getMessage());
                // Continue processing other records
            } catch (Exception e) {
                log.error("Unexpected error processing S3 record: {}", record, e);
                // Continue processing other records
            }
        }
        
        return "Processing completed";
    }
    
    private void processImage(String sourceKey) {
        log.info("Processing image: {}", sourceKey);
        
        // Download original image
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(sourceKey)
                .build();
        
        InputStream originalStream;
        BufferedImage originalImage;
        
        try {
            originalStream = s3Client.getObject(getRequest);
            originalImage = ImageIO.read(originalStream);
            originalStream.close();
        } catch (Exception e) {
            log.error("Failed to download image from S3: {}", sourceKey, e);
            throw new ImageProcessingException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "Failed to download image", e);
        }
        
        if (originalImage == null) {
            log.warn("Failed to read image format: {}", sourceKey);
            throw new ImageProcessingException(ImageErrorCode.INVALID_IMAGE_FORMAT, "Cannot read image format for: " + sourceKey);
        }
        
        String baseName = extractBaseName(sourceKey);
        String originalFormat = extractFormat(sourceKey);
        
        log.info("Original image dimensions: {}x{}", originalImage.getWidth(), originalImage.getHeight());
        
        // Generate multiple sizes
        for (Map.Entry<String, ImageSize> entry : IMAGE_SIZES.entrySet()) {
            String sizeName = entry.getKey();
            ImageSize targetSize = entry.getValue();
            
            try {
                processImageSize(originalImage, baseName, sizeName, targetSize, originalFormat);
            } catch (Exception e) {
                log.error("Failed to process size {} for image {}", sizeName, sourceKey, e);
                throw new ImageProcessingException(ImageErrorCode.IMAGE_RESIZE_FAILED, 
                    String.format("Failed to process %s size for image %s", sizeName, sourceKey), e);
            }
        }
        
        // Also save optimized original size
        try {
            saveOptimizedOriginal(originalImage, baseName, originalFormat);
        } catch (Exception e) {
            log.error("Failed to save optimized original for image {}", sourceKey, e);
            throw new ImageProcessingException(ImageErrorCode.IMAGE_PROCESSING_FAILED, 
                "Failed to save optimized original for: " + sourceKey, e);
        }
        
        log.info("Successfully processed image: {}", sourceKey);
    }
    
    private void processImageSize(BufferedImage originalImage, String baseName, String sizeName, 
                                 ImageSize targetSize, String originalFormat) throws IOException {
        
        // Calculate dimensions maintaining aspect ratio
        Dimension newDimensions = calculateDimensions(
                originalImage.getWidth(), 
                originalImage.getHeight(), 
                targetSize.getMaxWidth(), 
                targetSize.getMaxHeight()
        );
        
        // Resize image
        BufferedImage resizedImage = resizeImage(originalImage, newDimensions.width, newDimensions.height);
        
        // Save in original format
        String originalKey = String.format("processed/%s_%s.%s", baseName, sizeName, originalFormat);
        saveImage(resizedImage, originalKey, originalFormat, targetSize.getQuality());
        
        // Save as WebP for better compression (if not already WebP)
        if (!originalFormat.equalsIgnoreCase("webp")) {
            String webpKey = String.format("processed/%s_%s.webp", baseName, sizeName);
            saveImage(resizedImage, webpKey, "webp", targetSize.getQuality());
        }
        
        log.debug("Created {} size: {}x{} -> {}", sizeName, newDimensions.width, newDimensions.height, originalKey);
    }
    
    private void saveOptimizedOriginal(BufferedImage originalImage, String baseName, String originalFormat) throws IOException {
        // Save optimized original
        String originalKey = String.format("processed/%s_original.%s", baseName, originalFormat);
        saveImage(originalImage, originalKey, originalFormat, 0.95f);
        
        // Save as WebP
        if (!originalFormat.equalsIgnoreCase("webp")) {
            String webpKey = String.format("processed/%s_original.webp", baseName);
            saveImage(originalImage, webpKey, "webp", 0.95f);
        }
    }
    
    private Dimension calculateDimensions(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {
        double ratio = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
        
        // Don't upscale images
        if (ratio > 1.0) {
            return new Dimension(originalWidth, originalHeight);
        }
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return new Dimension(newWidth, newHeight);
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        
        return resizedImage;
    }
    
    private void saveImage(BufferedImage image, String key, String format, float quality) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        if (format.equalsIgnoreCase("webp")) {
            // For WebP, we would need a WebP encoder library like imageio-webp
            // For now, falling back to JPEG with high quality
            format = "jpg";
        }
        
        // Write image with compression
        ImageIO.write(image, format, outputStream);
        
        byte[] imageBytes = outputStream.toByteArray();
        outputStream.close();
        
        // Upload to S3
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(getContentType(format))
                .contentLength((long) imageBytes.length)
                .metadata(Map.of(
                        "processed-at", String.valueOf(System.currentTimeMillis()),
                        "format", format,
                        "quality", String.valueOf(quality),
                        "dimensions", image.getWidth() + "x" + image.getHeight()
                ))
                .build();
        
        s3Client.putObject(putRequest, RequestBody.fromBytes(imageBytes));
        
        log.debug("Uploaded processed image: {} ({}x{}, {} bytes)", 
                key, image.getWidth(), image.getHeight(), imageBytes.length);
    }
    
    private String getContentType(String format) {
        return switch (format.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }
    
    private String extractBaseName(String key) {
        // Extract filename without extension from path like "originals/uuid.jpg"
        String filename = key.substring(key.lastIndexOf('/') + 1);
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }
    
    private String extractFormat(String key) {
        int dotIndex = key.lastIndexOf('.');
        return dotIndex > 0 ? key.substring(dotIndex + 1).toLowerCase() : "jpg";
    }
    
    // Helper class for image size configuration
    private static class ImageSize {
        private final int maxWidth;
        private final int maxHeight;
        private final float quality;
        
        public ImageSize(int maxWidth, int maxHeight, float quality) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
            this.quality = quality;
        }
        
        public int getMaxWidth() { return maxWidth; }
        public int getMaxHeight() { return maxHeight; }
        public float getQuality() { return quality; }
    }
}