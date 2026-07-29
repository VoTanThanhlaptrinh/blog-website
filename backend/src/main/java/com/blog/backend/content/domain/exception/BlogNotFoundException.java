package com.blog.backend.content.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class BlogNotFoundException extends DomainException {
    public BlogNotFoundException(String message) {
        super(ContentErrorCode.BLOG_NOT_FOUND, message);
    }
}
