package com.blog.backend.identity.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class AccountAlreadyActiveException extends DomainException {
    public AccountAlreadyActiveException() {
        super(IdentityErrorCode.ACCOUNT_ALREADY_ACTIVE);
    }
    
    public AccountAlreadyActiveException(String message) {
        super(IdentityErrorCode.ACCOUNT_ALREADY_ACTIVE, message);
    }
}
