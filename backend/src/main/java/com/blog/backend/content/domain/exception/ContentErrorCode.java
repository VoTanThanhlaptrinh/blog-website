package com.blog.backend.content.domain.exception;

import com.blog.backend.notification.domain.exception.ErrorCode;

public enum ContentErrorCode implements ErrorCode {
    BLOG_NOT_FOUND(404, "Không tìm thấy bài viết"),
    UNAUTHORIZED_BLOG_ACCESS(403, "Bạn không có quyền thao tác trên bài viết này"),
    BLOG_ALREADY_DELETED(400, "Bài viết đã bị xóa và không thể chỉnh sửa");

    private final int code;
    private final String message;

    ContentErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
