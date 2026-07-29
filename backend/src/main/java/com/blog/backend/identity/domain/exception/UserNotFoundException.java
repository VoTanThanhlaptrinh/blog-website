package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(IdentityErrorCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(String message) {
        super(IdentityErrorCode.USER_NOT_FOUND, message);
    }
}
