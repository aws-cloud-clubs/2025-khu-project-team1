package com.khu.acc.newsfeed.common.exception.image;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;

/**
 * 이미지 관련 오류 코드
 */
public enum ImageErrorCode implements ErrorCode {
    
    // 업로드 요청 관련 오류
    INVALID_FILE_NAME("IMG001", "파일 이름이 유효하지 않습니다"),
    INVALID_CONTENT_TYPE("IMG002", "지원되지 않는 파일 형식입니다. JPEG, PNG, WebP만 지원됩니다"),
    INVALID_FILE_SIZE("IMG003", "파일 크기가 유효하지 않습니다"),
    FILE_SIZE_EXCEEDED("IMG004", "파일 크기가 최대 허용 크기(10MB)를 초과했습니다"),
    INVALID_FILE_EXTENSION("IMG005", "지원되지 않는 파일 확장자입니다. jpg, jpeg, png, webp만 지원됩니다"),
    
    // URL 검증 관련 오류
    INVALID_IMAGE_URL("IMG006", "유효하지 않은 이미지 URL입니다"),
    UNAUTHORIZED_IMAGE_URL("IMG007", "승인되지 않은 도메인의 이미지 URL입니다"),
    
    // S3 관련 오류
    S3_BUCKET_NOT_CONFIGURED("IMG008", "S3 버킷이 설정되지 않았습니다"),
    CLOUDFRONT_DOMAIN_NOT_CONFIGURED("IMG009", "CloudFront 도메인이 설정되지 않았습니다"),
    PRESIGNED_URL_GENERATION_FAILED("IMG010", "Presigned URL 생성에 실패했습니다"),
    
    // 이미지 처리 관련 오류
    IMAGE_PROCESSING_FAILED("IMG011", "이미지 처리에 실패했습니다"),
    INVALID_IMAGE_FORMAT("IMG012", "이미지 형식을 읽을 수 없습니다"),
    IMAGE_RESIZE_FAILED("IMG013", "이미지 리사이즈에 실패했습니다");

    private final String code;
    private final String message;

    ImageErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCodeWithMessage() {
        return String.format("[%s] %s", code, message);
    }
}