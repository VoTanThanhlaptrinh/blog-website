package com.blog.backend.notification.api.exception;

import com.blog.backend.notification.api.ApiResponse;
import com.blog.backend.notification.domain.exception.DomainException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        ApiResponse<Void> response = new ApiResponse<>(null, ex.getMessage(), ex.getErrorCode().getCode());
        return ResponseEntity.status(ex.getErrorCode().getCode()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        ApiResponse<Void> response = new ApiResponse<>(null, "Internal Server Error: " + ex.getMessage(), 500);
        return ResponseEntity.status(500).body(response);
    }
}
