package com.blog.backend.interaction.domain.exception;

import com.blog.backend.notification.domain.exception.ErrorCode;

public enum InteractionErrorCode implements ErrorCode {
    COMMENT_NOT_FOUND(404, "Không tìm thấy bình luận"),
    UNAUTHORIZED_COMMENT_ACCESS(403, "Bạn không có quyền thao tác trên bình luận này"),
    COMMENT_ALREADY_DELETED(400, "Bình luận đã bị xóa"),
    CANNOT_REPLY_TO_REPLY(400, "Không thể trả lời bình luận con (chỉ hỗ trợ reply 1 cấp)"),
    PARENT_COMMENT_NOT_FOUND(404, "Không tìm thấy bình luận cha");

    private final int code;
    private final String message;

    InteractionErrorCode(int code, String message) {
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
