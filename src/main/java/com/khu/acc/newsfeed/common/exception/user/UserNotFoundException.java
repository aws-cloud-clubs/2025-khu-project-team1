package com.khu.acc.newsfeed.common.exception.user;

import lombok.Getter;

@Getter
public class UserNotFoundException extends RuntimeException {

    private final UserErrorCode errorCode;
    private final String userId;

    public UserNotFoundException(String userId) {
        super(String.format("%s - User ID: %s", UserErrorCode.USER_NOT_FOUND.getCodeWithMessage(), userId));
        this.errorCode = UserErrorCode.USER_NOT_FOUND;
        this.userId = userId;
    }

    public UserNotFoundException(UserErrorCode errorCode) {
        super(errorCode.getCodeWithMessage());
        this.errorCode = errorCode;
        this.userId = null;
    }

    public UserNotFoundException(UserErrorCode errorCode, String userId) {
        super(String.format("%s - User ID: %s", errorCode.getCodeWithMessage(), userId));
        this.errorCode = errorCode;
        this.userId = userId;
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = UserErrorCode.USER_NOT_FOUND;
        this.userId = null;
    }
}