package com.khu.acc.newsfeed.common.exception.user;

import com.khu.acc.newsfeed.common.exception.common.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * User Aggregate 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    
    // Domain Layer Errors (U001-U099)
    USER_EMAIL_INVALID("U001", "Invalid email format"),
    USER_PASSWORD_TOO_WEAK("U002", "Password does not meet security requirements"),
    USER_USERNAME_INVALID("U003", "Username contains invalid characters"),
    
    // Application Layer Errors (U100-U399)
    USER_NOT_FOUND("U100", "User not found"),
    USER_INACTIVE("U101", "User account is inactive"),
    USER_ALREADY_EXISTS("U102", "User already exists with this email"),
    USER_ACCESS_DENIED("U103", "Access denied to user resource"),
    
    // Operation Errors (U400-U499)
    USER_CREATION_FAILED("U400", "Failed to create user"),
    USER_UPDATE_FAILED("U401", "Failed to update user"),
    USER_DELETE_FAILED("U402", "Failed to delete user");
    
    private final String code;
    private final String message;
}