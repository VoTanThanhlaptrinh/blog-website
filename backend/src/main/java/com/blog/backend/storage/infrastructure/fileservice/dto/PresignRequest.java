package com.blog.backend.storage.infrastructure.fileservice.dto;

public record PresignRequest(String context, FileItem file) {

    public record FileItem(String fileName, String mimeType, long size) {
    }
}
