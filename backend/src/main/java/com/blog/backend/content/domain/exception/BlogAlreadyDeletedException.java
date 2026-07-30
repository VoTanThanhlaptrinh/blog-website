package com.blog.backend.content.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class BlogAlreadyDeletedException extends DomainException {
    public BlogAlreadyDeletedException(String message) {
        super(ContentErrorCode.BLOG_ALREADY_DELETED, message);
    }
}
