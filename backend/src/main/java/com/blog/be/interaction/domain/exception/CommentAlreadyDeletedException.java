package com.blog.be.interaction.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class CommentAlreadyDeletedException extends DomainException {
    public CommentAlreadyDeletedException(String message) {
        super(InteractionErrorCode.COMMENT_ALREADY_DELETED, message);
    }
}
