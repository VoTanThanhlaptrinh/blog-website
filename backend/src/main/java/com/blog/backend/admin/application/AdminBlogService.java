package com.blog.backend.admin.application;

import com.blog.backend.admin.api.dto.RejectBlogRequest;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.identity.domain.entity.User;
import org.springframework.data.domain.Pageable;

public interface AdminBlogService {
    PageResponse<BlogResponse> getBlogsForModeration(BlogStatus status, String keyword, Pageable pageable, User adminUser);
    BlogResponse approveBlog(Long blogId, User adminUser);
    BlogResponse rejectBlog(Long blogId, RejectBlogRequest request, User adminUser);
    byte[] exportBlogsCsv(BlogStatus status, String keyword, User adminUser);
}
