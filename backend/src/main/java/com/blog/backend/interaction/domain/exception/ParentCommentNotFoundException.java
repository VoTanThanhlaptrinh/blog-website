package com.blog.backend.interaction.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class ParentCommentNotFoundException extends DomainException {
    public ParentCommentNotFoundException(String message) {
        super(InteractionErrorCode.PARENT_COMMENT_NOT_FOUND, message);
    }
}
