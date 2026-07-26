package com.blog.be.content.application;

import com.blog.be.content.api.dto.*;
import com.blog.be.content.domain.enums.BlogStatus;
import org.springframework.data.domain.Pageable;

import java.security.Principal;

public interface BlogService {
    BlogResponse createBlog(Principal principal, CreateBlogRequest request);
    BlogResponse getBlogById(Long id, Principal principal);
    PageResponse<BlogResponse> getBlogs(String keyword, BlogStatus status, Long userId, Pageable pageable, Principal principal);
    BlogResponse updateBlog(Long id, Principal principal, UpdateBlogRequest request);
    void deleteBlog(Long id, Principal principal);
}
