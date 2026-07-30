package com.blog.backend.interaction.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class UnauthorizedCommentAccessException extends DomainException {
    public UnauthorizedCommentAccessException(String message) {
        super(InteractionErrorCode.UNAUTHORIZED_COMMENT_ACCESS, message);
    }
}
