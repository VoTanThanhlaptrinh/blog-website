package com.blog.be.content.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class BlogAlreadyDeletedException extends DomainException {
    public BlogAlreadyDeletedException(String message) {
        super(ContentErrorCode.BLOG_ALREADY_DELETED, message);
    }
}
