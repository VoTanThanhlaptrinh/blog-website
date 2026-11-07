package com.blog.backend.storage.domain.exception;

import com.blog.backend.notification.domain.exception.ErrorCode;

public enum StorageErrorCode implements ErrorCode {
    INVALID_UPLOAD(400, "Yêu cầu upload không hợp lệ"),
    FILE_SERVICE_MISCONFIGURED(500, "Cấu hình file-service không hợp lệ"),
    FILE_SERVICE_UNAVAILABLE(503, "Dịch vụ lưu trữ file tạm thời không khả dụng");

    private final int code;
    private final String message;

    StorageErrorCode(int code, String message) {
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
