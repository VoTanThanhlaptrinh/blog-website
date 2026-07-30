package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class IncorrectPasswordException extends DomainException {
    public IncorrectPasswordException() {
        super(IdentityErrorCode.INCORRECT_OLD_PASSWORD);
    }
    
    public IncorrectPasswordException(String message) {
        super(IdentityErrorCode.INCORRECT_OLD_PASSWORD, message);
    }
}
