package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class ExpiredOtpException extends DomainException {
    public ExpiredOtpException(String message) {
        super(IdentityErrorCode.EXPIRED_OTP, message);
    }
}
