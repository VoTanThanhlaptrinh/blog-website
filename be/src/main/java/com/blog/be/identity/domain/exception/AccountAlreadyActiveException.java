package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class AccountAlreadyActiveException extends DomainException {
    public AccountAlreadyActiveException() {
        super(IdentityErrorCode.ACCOUNT_ALREADY_ACTIVE);
    }
    
    public AccountAlreadyActiveException(String message) {
        super(IdentityErrorCode.ACCOUNT_ALREADY_ACTIVE, message);
    }
}
