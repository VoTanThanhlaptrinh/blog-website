package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class ExpiredOtpException extends DomainException {
    public ExpiredOtpException(String message) {
        super(IdentityErrorCode.EXPIRED_OTP, message);
    }
}
