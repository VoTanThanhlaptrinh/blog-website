package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class InvalidTokenException extends DomainException {
    public InvalidTokenException() {
        super(IdentityErrorCode.INVALID_TOKEN);
    }
    
    public InvalidTokenException(String message) {
        super(IdentityErrorCode.INVALID_TOKEN, message);
    }
}
