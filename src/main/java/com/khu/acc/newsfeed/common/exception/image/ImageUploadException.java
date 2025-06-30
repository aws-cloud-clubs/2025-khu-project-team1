package com.khu.acc.newsfeed.common.exception.image;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;

/**
 * 이미지 업로드 관련 예외
 */
public class ImageUploadException extends ApplicationException {

    public ImageUploadException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageUploadException(ImageErrorCode errorCode, String additionalMessage) {
        super(errorCode, additionalMessage);
    }

    public ImageUploadException(ImageErrorCode errorCode, Throwable cause) {
        super(errorCode, null, cause);
    }

    public ImageUploadException(ImageErrorCode errorCode, String additionalMessage, Throwable cause) {
        super(errorCode, additionalMessage, cause);
    }
}