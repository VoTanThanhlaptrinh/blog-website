package com.blog.be.admin.application;

import com.blog.be.admin.api.dto.*;
import com.blog.be.admin.domain.enums.ReportStatus;
import com.blog.be.admin.domain.enums.ReportTargetType;
import com.blog.be.content.api.dto.BlogResponse;
import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.identity.domain.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminService {
    // Content Moderation
    PageResponse<BlogResponse> getBlogsForModeration(BlogStatus status, String keyword, Pageable pageable, User adminUser);
    BlogResponse approveBlog(Long blogId, User adminUser);
    BlogResponse rejectBlog(Long blogId, RejectBlogRequest request, User adminUser);

    // Report Management
    ReportResponse createReport(User reporter, CreateReportRequest request);
    PageResponse<ReportResponse> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable, User adminUser);
    ReportResponse resolveReport(Long reportId, ResolveReportRequest request, User adminUser);

    // Analytics Dashboard
    AdminDashboardSummaryResponse getDashboardSummary(User adminUser);
    List<DailyGrowthResponse> getGrowthStats(int days, User adminUser);
    List<BlogResponse> getTopBlogs(int limit, User adminUser);
}
