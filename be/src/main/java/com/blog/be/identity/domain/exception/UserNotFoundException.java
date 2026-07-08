package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(IdentityErrorCode.USER_NOT_FOUND);
    }
    
    public UserNotFoundException(String message) {
        super(IdentityErrorCode.USER_NOT_FOUND, message);
    }
}
