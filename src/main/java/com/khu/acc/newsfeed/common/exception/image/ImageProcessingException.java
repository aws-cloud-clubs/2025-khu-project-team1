package com.khu.acc.newsfeed.common.exception.image;

import com.khu.acc.newsfeed.common.exception.application.ApplicationException;

/**
 * 이미지 처리 관련 예외
 */
public class ImageProcessingException extends ApplicationException {

    public ImageProcessingException(ImageErrorCode errorCode) {
        super(errorCode);
    }

    public ImageProcessingException(ImageErrorCode errorCode, String additionalMessage) {
        super(errorCode, additionalMessage);
    }

    public ImageProcessingException(ImageErrorCode errorCode, Throwable cause) {
        super(errorCode, null, cause);
    }

    public ImageProcessingException(ImageErrorCode errorCode, String additionalMessage, Throwable cause) {
        super(errorCode, additionalMessage, cause);
    }
}