package com.blog.backend.storage.infrastructure.fileservice.dto;

/** Envelope used by every file-service response: { success, data } or { success: false, message }. */
public record FileServiceResponse<T>(boolean success, T data, String message) {
}
