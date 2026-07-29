package com.blog.be.admin.api.dto;

import com.blog.be.admin.domain.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReportRequest {
    @NotNull(message = "Loại đối tượng báo cáo không được để trống")
    private ReportTargetType targetType;

    @NotNull(message = "ID đối tượng báo cáo không được để trống")
    private Long targetId;

    @NotBlank(message = "Lý do báo cáo không được để trống")
    private String reason;
}
