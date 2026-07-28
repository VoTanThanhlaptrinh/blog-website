package com.blog.be.interaction.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class CommentNotFoundException extends DomainException {
    public CommentNotFoundException(String message) {
        super(InteractionErrorCode.COMMENT_NOT_FOUND, message);
    }
}
