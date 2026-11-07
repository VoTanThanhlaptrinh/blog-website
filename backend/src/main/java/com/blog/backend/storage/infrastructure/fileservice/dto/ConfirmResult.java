package com.blog.backend.storage.infrastructure.fileservice.dto;

/** url is the permanent public URL, or null for private files. */
public record ConfirmResult(String key, String visibility, String url) {
}
