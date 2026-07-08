package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class PasswordMismatchException extends DomainException {
    public PasswordMismatchException(String message) {
        super(IdentityErrorCode.PASSWORD_MISMATCH, message);
    }
}
