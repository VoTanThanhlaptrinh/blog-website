package com.blog.backend.admin.application.impl;

import com.blog.backend.admin.api.dto.RejectBlogRequest;
import com.blog.backend.admin.application.AdminBlogService;
import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.CategoryResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.event.BlogModerationEvent;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.domain.repository.CommentRepository;
import com.blog.backend.interaction.domain.repository.LikeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminBlogServiceImpl implements AdminBlogService {

    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    private void validateAdmin(User adminUser) {
        if (adminUser == null || adminUser.getAuthorities() == null ||
                adminUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedBlogAccessException("Chỉ có Quản trị viên mới được phép thực hiện thao tác này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogResponse> getBlogsForModeration(BlogStatus status, String keyword, Pageable pageable, User adminUser) {
        validateAdmin(adminUser);

        Specification<Blog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                predicates.add(cb.equal(root.get("status"), BlogStatus.PENDING));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate p1 = cb.like(cb.lower(root.get("title")), pattern);
                Predicate p2 = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Blog> pageResult = blogRepository.findAll(spec, pageable);
        List<BlogResponse> content = pageResult.getContent().stream()
                .map(this::mapToBlogResponse)
                .collect(Collectors.toList());

        return PageResponse.<BlogResponse>builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .last(pageResult.isLast())
                .build();
    }

    @Override
    @Transactional
    public BlogResponse approveBlog(Long blogId, User adminUser) {
        validateAdmin(adminUser);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + blogId));

        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setRejectionReason(null);
        blog = blogRepository.save(blog);

        eventPublisher.publishEvent(new BlogModerationEvent(this, blog, BlogStatus.PUBLISHED, null));

        return mapToBlogResponse(blog);
    }

    @Override
    @Transactional
    public BlogResponse rejectBlog(Long blogId, RejectBlogRequest request, User adminUser) {
        validateAdmin(adminUser);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + blogId));

        blog.setStatus(BlogStatus.REJECTED);
        blog.setRejectionReason(request.getReason());
        blog = blogRepository.save(blog);

        eventPublisher.publishEvent(new BlogModerationEvent(this, blog, BlogStatus.REJECTED, request.getReason()));

        return mapToBlogResponse(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportBlogsCsv(BlogStatus status, String keyword, User adminUser) {
        validateAdmin(adminUser);
        List<Blog> blogs = blogRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,AuthorEmail,Status,ViewsCount,CreatedDate\n");
        for (Blog b : blogs) {
            sb.append(b.getId()).append(",")
              .append("\"").append(b.getTitle() != null ? b.getTitle().replace("\"", "\"\"") : "").append("\",")
              .append(b.getUser() != null ? b.getUser().getEmail() : "").append(",")
              .append(b.getStatus() != null ? b.getStatus() : "").append(",")
              .append(b.getViewCount()).append(",")
              .append(b.getCreatedDate() != null ? b.getCreatedDate() : "").append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private BlogResponse mapToBlogResponse(Blog blog) {
        AuthorResponse authorResponse = null;
        if (blog.getUser() != null) {
            authorResponse = AuthorResponse.builder()
                    .id(blog.getUser().getId())
                    .email(blog.getUser().getEmail())
                    .bio(blog.getUser().getBio())
                    .avatarUrl(blog.getUser().getAvatar() != null ? blog.getUser().getAvatar().getUrl() : null)
                    .build();
        }

        CategoryResponse categoryResponse = null;
        if (blog.getCategory() != null) {
            com.blog.backend.content.domain.entity.Category c = blog.getCategory();
            categoryResponse = CategoryResponse.builder()
                    .id(c.getId())
                    .name(c.getName())
                    .slug(c.getSlug())
                    .description(c.getDescription())
                    .build();
        }

        int likesCount = (int) likeRepository.countByBlogIdAndLikedTrue(blog.getId());
        int commentsCount = (int) commentRepository.countByBlogIdAndStatusNot(blog.getId(), com.blog.backend.interaction.domain.enums.CommentStatus.DELETED);

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .thumbnailUrl(blog.getThumbnailUrl())
                .status(blog.getStatus())
                .rejectionReason(blog.getRejectionReason())
                .viewsCount(blog.getViewCount())
                .sharesCount(blog.getShareCount())
                .likesCount(likesCount)
                .commentsCount(commentsCount)
                .category(categoryResponse)
                .author(authorResponse)
                .createdDate(blog.getCreatedDate())
                .modifiedDate(blog.getModifiedDate())
                .build();
    }
}
