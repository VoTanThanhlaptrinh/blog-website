package com.blog.be.admin.api.dto;

import com.blog.be.admin.domain.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResolveReportRequest {
    @NotNull(message = "Trạng thái xử lý không được để trống")
    private ReportStatus status;

    private String adminNotes;
}
