package com.blog.be.content.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class BlogNotFoundException extends DomainException {
    public BlogNotFoundException(String message) {
        super(ContentErrorCode.BLOG_NOT_FOUND, message);
    }
}
