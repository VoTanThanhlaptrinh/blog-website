package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class InvalidOtpException extends DomainException {
    public InvalidOtpException(String message) {
        super(IdentityErrorCode.INVALID_OTP, message);
    }
}
