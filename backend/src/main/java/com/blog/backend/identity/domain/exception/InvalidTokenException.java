package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class InvalidTokenException extends DomainException {
    public InvalidTokenException() {
        super(IdentityErrorCode.INVALID_TOKEN);
    }
    
    public InvalidTokenException(String message) {
        super(IdentityErrorCode.INVALID_TOKEN, message);
    }
}
