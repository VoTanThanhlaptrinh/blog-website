package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class InvalidResetTokenException extends DomainException {
    public InvalidResetTokenException(String message) {
        super(IdentityErrorCode.INVALID_RESET_TOKEN, message);
    }
}
