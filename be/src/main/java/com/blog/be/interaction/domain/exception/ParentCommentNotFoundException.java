package com.blog.be.interaction.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class ParentCommentNotFoundException extends DomainException {
    public ParentCommentNotFoundException(String message) {
        super(InteractionErrorCode.PARENT_COMMENT_NOT_FOUND, message);
    }
}
