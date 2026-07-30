package com.blog.backend.interaction.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class CannotReplyToReplyException extends DomainException {
    public CannotReplyToReplyException(String message) {
        super(InteractionErrorCode.CANNOT_REPLY_TO_REPLY, message);
    }
}
