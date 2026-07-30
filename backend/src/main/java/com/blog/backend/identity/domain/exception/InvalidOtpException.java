package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class InvalidOtpException extends DomainException {
    public InvalidOtpException(String message) {
        super(IdentityErrorCode.INVALID_OTP, message);
    }
}
