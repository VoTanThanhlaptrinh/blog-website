package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class UsernameAlreadyExistsException extends DomainException {
    public UsernameAlreadyExistsException() {
        super(IdentityErrorCode.USER_ALREADY_EXISTS);
    }
    
    public UsernameAlreadyExistsException(String message) {
        super(IdentityErrorCode.USER_ALREADY_EXISTS, message);
    }
}
