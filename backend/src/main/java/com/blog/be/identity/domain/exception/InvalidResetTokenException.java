package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class InvalidResetTokenException extends DomainException {
    public InvalidResetTokenException(String message) {
        super(IdentityErrorCode.INVALID_RESET_TOKEN, message);
    }
}
