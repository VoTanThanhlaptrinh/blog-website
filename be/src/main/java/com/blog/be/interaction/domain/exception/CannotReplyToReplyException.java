package com.blog.be.interaction.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class CannotReplyToReplyException extends DomainException {
    public CannotReplyToReplyException(String message) {
        super(InteractionErrorCode.CANNOT_REPLY_TO_REPLY, message);
    }
}
