package com.khu.acc.newsfeed.common.exception.user;

import com.khu.acc.newsfeed.common.exception.domain.DomainException;

public class UserInactiveException extends DomainException {
    
    public UserInactiveException(String userId) {
        super(UserErrorCode.USER_INACTIVE, "User is inactive: " + userId);
    }
    
    public UserInactiveException(String userId, String message) {
        super(UserErrorCode.USER_INACTIVE, message);
    }
}