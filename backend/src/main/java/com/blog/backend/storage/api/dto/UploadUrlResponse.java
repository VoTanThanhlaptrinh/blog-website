package com.blog.backend.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadUrlResponse {
    private String presignedUrl;
    private String objectKey; // Trả về key để client gửi lại backend sau khi upload xong
}
