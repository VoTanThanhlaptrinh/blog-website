package com.blog.be.content.domain.exception;

import com.blog.be.notification.domain.exception.DomainException;

public class UnauthorizedBlogAccessException extends DomainException {
    public UnauthorizedBlogAccessException(String message) {
        super(ContentErrorCode.UNAUTHORIZED_BLOG_ACCESS, message);
    }
}
