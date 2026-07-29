package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class UsernameAlreadyExistsException extends DomainException {
    public UsernameAlreadyExistsException() {
        super(IdentityErrorCode.USER_ALREADY_EXISTS);
    }
    
    public UsernameAlreadyExistsException(String message) {
        super(IdentityErrorCode.USER_ALREADY_EXISTS, message);
    }
}
