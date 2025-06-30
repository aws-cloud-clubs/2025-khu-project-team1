package com.khu.acc.newsfeed.common.exception.image;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;

/**
 * 이미지 검증 관련 예외
 */
public class ImageValidationException extends ApplicationException {

    public ImageValidationException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageValidationException(ImageErrorCode errorCode, String additionalMessage) {
        super(errorCode, additionalMessage);
    }

    public ImageValidationException(ImageErrorCode errorCode, Throwable cause) {
        super(errorCode, null, cause);
    }

    public ImageValidationException(ImageErrorCode errorCode, String additionalMessage, Throwable cause) {
        super(errorCode, additionalMessage, cause);
    }
}