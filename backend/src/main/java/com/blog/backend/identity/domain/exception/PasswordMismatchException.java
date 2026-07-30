package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class PasswordMismatchException extends DomainException {
    public PasswordMismatchException(String message) {
        super(IdentityErrorCode.PASSWORD_MISMATCH, message);
    }
}
