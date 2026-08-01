package com.blog.backend.admin.application;

import com.blog.backend.admin.api.dto.AdminDashboardSummaryResponse;
import com.blog.backend.admin.api.dto.DailyGrowthResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.identity.domain.entity.User;
import java.util.List;

public interface AdminDashboardService {
    AdminDashboardSummaryResponse getDashboardSummary(User adminUser);
    List<DailyGrowthResponse> getGrowthStats(int days, User adminUser);
    List<BlogResponse> getTopBlogs(int limit, User adminUser);
}
