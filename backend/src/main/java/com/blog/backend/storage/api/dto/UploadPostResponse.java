package com.blog.backend.storage.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UploadPostResponse {
    private String uploadUrl;     // URL của R2 (https://<account_id>.r2.cloudflarestorage.com/<bucket>)
    private String objectKey;     // Key chính xác để backend biết file lưu ở đâu
    private Map<String, String> formData; // Các trường bảo mật frontend cần append vào form
    private String publicUrl;     // Link CDN hiển thị công khai
}
