package com.blog.be.storage.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UploadUrlRequest {
    @NotBlank(message = "Tên file không được để trống")
    private String fileName;
    
    @NotBlank(message = "Loại file (contentType) không được để trống")
    private String contentType;
    
    @NotBlank(message = "Thư mục không được để trống")
    private String folder;
    
    @Min(value = 1, message = "Kích thước file phải lớn hơn 0")
    private long fileSize;
}
