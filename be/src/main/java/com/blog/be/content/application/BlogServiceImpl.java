package com.blog.be.content.application;

import com.blog.be.content.api.dto.*;
import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.enums.BlogStatus;
import com.blog.be.content.domain.exception.BlogAlreadyDeletedException;
import com.blog.be.content.domain.exception.BlogNotFoundException;
import com.blog.be.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.User;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
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
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    @Override
    @Transactional
    public BlogResponse createBlog(User currentUser, CreateBlogRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để tạo bài viết");
        }

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .status(request.getStatus() != null ? request.getStatus() : BlogStatus.DRAFT)
                .user(currentUser)
                .build();

        blog = blogRepository.save(blog);
        return mapToResponse(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogResponse getBlogById(Long id, User currentUser) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + id));

        if (blog.getStatus() != BlogStatus.PUBLISHED) {
            if (!isAuthorOrAdmin(currentUser, blog)) {
                throw new UnauthorizedBlogAccessException("Bạn không có quyền xem bài viết này hoặc bài viết chưa được xuất bản");
            }
        }

        return mapToResponse(blog);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogResponse> getBlogs(String keyword, BlogStatus status, Long userId, Pageable pageable, User currentUser) {
        Specification<Blog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                if (status != BlogStatus.PUBLISHED) {
                    if (currentUser == null || currentUser.getId() == null) {
                        throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để xem các bài viết ở trạng thái " + status);
                    }
                    boolean isAdmin = isAdmin(currentUser);
                    if (!isAdmin && (userId == null || !userId.equals(currentUser.getId()))) {
                        predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
                    } else if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                    predicates.add(cb.equal(root.get("status"), status));
                } else {
                    predicates.add(cb.equal(root.get("status"), BlogStatus.PUBLISHED));
                    if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                }
            } else {
                if (currentUser != null && userId != null && userId.equals(currentUser.getId())) {
                    predicates.add(cb.notEqual(root.get("status"), BlogStatus.DELETED));
                    predicates.add(cb.equal(root.get("user").get("id"), userId));
                } else {
                    predicates.add(cb.equal(root.get("status"), BlogStatus.PUBLISHED));
                    if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                }
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(titlePredicate, descPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Blog> pageResult = blogRepository.findAll(spec, pageable);
        List<BlogResponse> content = pageResult.getContent().stream()
                .map(this::mapToResponse)
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
    public BlogResponse updateBlog(Long id, User currentUser, UpdateBlogRequest request) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + id));

        if (blog.getStatus() == BlogStatus.DELETED) {
            throw new BlogAlreadyDeletedException("Bài viết đã bị xóa mềm và không thể chỉnh sửa");
        }

        if (!isAuthorOrAdmin(currentUser, blog)) {
            throw new UnauthorizedBlogAccessException("Bạn không có quyền chỉnh sửa bài viết này");
        }

        if (request.getTitle() != null) blog.setTitle(request.getTitle());
        if (request.getDescription() != null) blog.setDescription(request.getDescription());
        if (request.getContent() != null) blog.setContent(request.getContent());
        if (request.getStatus() != null) blog.setStatus(request.getStatus());

        blog = blogRepository.save(blog);
        return mapToResponse(blog);
    }

    @Override
    @Transactional
    public void deleteBlog(Long id, User currentUser) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + id));

        if (blog.getStatus() == BlogStatus.DELETED) {
            return;
        }

        if (!isAuthorOrAdmin(currentUser, blog)) {
            throw new UnauthorizedBlogAccessException("Bạn không có quyền xóa bài viết này");
        }

        blog.setStatus(BlogStatus.DELETED);
        blogRepository.save(blog);
    }

    private boolean isAuthorOrAdmin(User currentUser, Blog blog) {
        if (currentUser == null || currentUser.getId() == null) return false;
        boolean isAuthor = blog.getUser() != null && blog.getUser().getId().equals(currentUser.getId());
        return isAuthor || isAdmin(currentUser);
    }

    private boolean isAdmin(User user) {
        if (user == null || user.getAuthorities() == null) return false;
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "ADMIN".equals(auth.getAuthority()));
    }

    private BlogResponse mapToResponse(Blog blog) {
        AuthorResponse authorResponse = null;
        if (blog.getUser() != null) {
            User u = blog.getUser();
            authorResponse = AuthorResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .avatarUrl(u.getAvatar() != null ? u.getAvatar().getUrl() : null)
                    .bio(u.getBio())
                    .build();
        }
        int likesCount = (blog.getLikes() != null) ? blog.getLikes().size() : 0;
        int commentsCount = (blog.getComments() != null) ? blog.getComments().size() : 0;

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .status(blog.getStatus())
                .author(authorResponse)
                .likesCount(likesCount)
                .commentsCount(commentsCount)
                .createdDate(blog.getCreatedDate())
                .modifiedDate(blog.getModifiedDate())
                .build();
    }
}
