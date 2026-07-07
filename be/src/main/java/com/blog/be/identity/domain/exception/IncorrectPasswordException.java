package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class IncorrectPasswordException extends DomainException {
    public IncorrectPasswordException() {
        super(IdentityErrorCode.INCORRECT_OLD_PASSWORD);
    }
    
    public IncorrectPasswordException(String message) {
        super(IdentityErrorCode.INCORRECT_OLD_PASSWORD, message);
    }
}
