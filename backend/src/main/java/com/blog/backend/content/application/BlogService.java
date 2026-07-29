package com.blog.be.content.application;

import com.blog.be.content.api.dto.*;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.identity.domain.entity.User;
import org.springframework.data.domain.Pageable;

public interface BlogService {
    BlogResponse createBlog(User currentUser, CreateBlogRequest request);
    BlogResponse getBlogById(Long id, User currentUser);
    PageResponse<BlogResponse> getBlogs(String keyword, BlogStatus status, Long userId, Long categoryId, Pageable pageable, User currentUser);
    BlogResponse updateBlog(Long id, User currentUser, UpdateBlogRequest request);
    void deleteBlog(Long id, User currentUser);
}
