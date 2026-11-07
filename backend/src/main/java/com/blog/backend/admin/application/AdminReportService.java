package com.blog.backend.admin.application;

import com.blog.backend.admin.api.dto.CreateReportRequest;
import com.blog.backend.admin.api.dto.PenalizeUserRequest;
import com.blog.backend.admin.api.dto.ReportResponse;
import com.blog.backend.admin.api.dto.ResolveReportRequest;
import com.blog.backend.admin.domain.enums.ReportStatus;
import com.blog.backend.admin.domain.enums.ReportTargetType;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import org.springframework.data.domain.Pageable;

public interface AdminReportService {
    ReportResponse createReport(User reporter, CreateReportRequest request);
    PageResponse<ReportResponse> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable, User adminUser);
    ReportResponse resolveReport(Long reportId, ResolveReportRequest request, User adminUser);
    ReportResponse penalizeUser(Long reportId, PenalizeUserRequest request, User adminUser);
    byte[] exportReportsCsv(ReportTargetType targetType, ReportStatus status, User adminUser);
}
