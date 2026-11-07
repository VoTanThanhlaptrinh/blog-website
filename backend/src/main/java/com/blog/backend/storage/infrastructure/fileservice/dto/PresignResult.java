package com.blog.backend.storage.infrastructure.fileservice.dto;

/** previewUrl is only set for public files; it points at the temp copy and must not be persisted. */
public record PresignResult(String originalName, String tempKey, String uploadUrl, int expiresIn, String previewUrl) {
}
