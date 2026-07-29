package com.blog.be.interaction.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class UnauthorizedCommentAccessException extends DomainException {
    public UnauthorizedCommentAccessException(String message) {
        super(InteractionErrorCode.UNAUTHORIZED_COMMENT_ACCESS, message);
    }
}
