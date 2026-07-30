package com.blog.backend.admin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponse {
    private long totalUsers;
    private long totalBlogs;
    private long totalComments;
    private long pendingBlogsCount;
    private long pendingReportsCount;
}
