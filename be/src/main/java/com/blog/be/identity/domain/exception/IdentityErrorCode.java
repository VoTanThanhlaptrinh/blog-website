package com.blog.be.identity.domain.exception;

import com.blog.be.notification.domain.exception.ErrorCode;

public enum IdentityErrorCode implements ErrorCode {
    USER_ALREADY_EXISTS(400, "Username is already taken"),
    INVALID_TOKEN(401, "Invalid or expired token"),
    USER_NOT_FOUND(404, "User not found"),
    ACCOUNT_ALREADY_ACTIVE(400, "Account is already active"),
    INCORRECT_OLD_PASSWORD(400, "Incorrect old password");

    private final int code;
    private final String message;

    IdentityErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
