package com.blog.be.admin.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectBlogRequest {
    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}
