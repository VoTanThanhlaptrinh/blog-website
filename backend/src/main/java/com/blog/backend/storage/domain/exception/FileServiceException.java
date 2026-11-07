package com.blog.backend.storage.domain.exception;

import com.blog.backend.notification.domain.exception.DomainException;

public class FileServiceException extends DomainException {
    public FileServiceException(StorageErrorCode errorCode) {
        super(errorCode);
    }

    public FileServiceException(StorageErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}
