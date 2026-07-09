package com.blog.be.storage.api.dto;

import lombok.Data;

@Data
public class UploadUrlRequest {
    private String fileName;
    private String contentType;
    private String folder;
    private long fileSize;
}
