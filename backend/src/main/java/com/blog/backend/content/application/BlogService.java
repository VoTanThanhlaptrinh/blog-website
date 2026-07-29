package com.blog.backend.content.application;

import com.blog.backend.content.api.dto.*;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.identity.domain.entity.User;
import org.springframework.data.domain.Pageable;

public interface BlogService {
    BlogResponse createBlog(User currentUser, CreateBlogRequest request);
    BlogResponse getBlogById(Long id, User currentUser);
    PageResponse<BlogResponse> getBlogs(String keyword, BlogStatus status, Long userId, Long categoryId, Pageable pageable, User currentUser);
    BlogResponse updateBlog(Long id, User currentUser, UpdateBlogRequest request);
    void deleteBlog(Long id, User currentUser);
}
