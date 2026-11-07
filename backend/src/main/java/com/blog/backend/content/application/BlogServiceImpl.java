package com.blog.backend.content.application;

import com.blog.backend.content.api.dto.*;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.event.BlogImagesActivatedEvent;
import com.blog.backend.content.domain.exception.BlogAlreadyDeletedException;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.content.domain.repository.CategoryRepository;
import com.blog.backend.identity.domain.entity.User;
import jakarta.persistence.criteria.Predicate;
import com.blog.backend.interaction.domain.enums.CommentStatus;
import com.blog.backend.interaction.domain.repository.CommentRepository;
import com.blog.backend.interaction.domain.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    /** URL of an image still in file-service's temp/ area (stops at markdown/HTML delimiters). */
    private static final Pattern TEMP_IMAGE_URL =
            Pattern.compile("https?://[^\\s()\"'<>\\[\\]]+/temp/public/blog/[^\\s()\"'<>\\[\\]]+");

    private final BlogRepository blogRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final CategoryRepository categoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public BlogResponse createBlog(User currentUser, CreateBlogRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để tạo bài viết");
        }

        BlogStatus targetStatus = request.getStatus();
        if (targetStatus == null) {
            targetStatus = BlogStatus.PENDING;
        } else if (targetStatus == BlogStatus.PUBLISHED && !isAdmin(currentUser)) {
            targetStatus = BlogStatus.PENDING;
        }

        com.blog.backend.content.domain.entity.Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId()).orElse(null);
        }

        Map<String, String> confirmedUrls = confirmTempImages(request.getContent(), request.getThumbnailUrl());

        Blog blog = Blog.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .content(replaceUrls(request.getContent(), confirmedUrls))
                .thumbnailUrl(replaceUrls(request.getThumbnailUrl(), confirmedUrls))
                .status(targetStatus)
                .user(currentUser)
                .category(category)
                .build();

        blog = blogRepository.save(blog);
        return mapToResponse(blog, currentUser);
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

        return mapToResponse(blog, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogResponse> getBlogs(String keyword, BlogStatus status, Long userId, Long categoryId, Pageable pageable, User currentUser) {
        Specification<Blog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            boolean isAuthenticated = currentUser != null && currentUser.getId() != null;
            boolean isAdmin = isAdmin(currentUser);
            boolean isFetchingOwnBlogs = isAuthenticated && userId != null && userId.equals(currentUser.getId());

            if (status != null) {
                // 1. Trường hợp có truyền status cụ thể
                if (status != BlogStatus.PUBLISHED) {
                    if (!isAuthenticated) {
                        throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để xem các bài viết ở trạng thái " + status);
                    }
                    // User thường chỉ được xem bài không phải PUBLISHED của chính mình
                    if (!isAdmin && !isFetchingOwnBlogs) {
                        predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));
                    } else if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                } else {
                    // Bài PUBLISHED thì ai cũng xem được
                    if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                }
                predicates.add(cb.equal(root.get("status"), status));
            } else {
                // 2. Trường hợp lấy danh sách chung (không truyền status)
                if (isFetchingOwnBlogs) {
                    // Lấy bài của chính mình -> Lấy mọi trạng thái trừ DELETED
                    predicates.add(cb.notEqual(root.get("status"), BlogStatus.DELETED));
                    predicates.add(cb.equal(root.get("user").get("id"), userId));
                } else {
                    // Xem bài người khác hoặc xem chung -> Chỉ lấy PUBLISHED
                    predicates.add(cb.equal(root.get("status"), BlogStatus.PUBLISHED));
                    if (userId != null) {
                        predicates.add(cb.equal(root.get("user").get("id"), userId));
                    }
                }
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
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
                .map(b -> mapToResponse(b, currentUser))
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
        Map<String, String> confirmedUrls = confirmTempImages(request.getContent(), request.getThumbnailUrl());
        if (request.getContent() != null) blog.setContent(replaceUrls(request.getContent(), confirmedUrls));
        if (request.getThumbnailUrl() != null) blog.setThumbnailUrl(replaceUrls(request.getThumbnailUrl(), confirmedUrls));
        if (request.getStatus() != null) {
            BlogStatus targetStatus = request.getStatus();
            if (targetStatus == BlogStatus.PUBLISHED && !isAdmin(currentUser)) {
                targetStatus = BlogStatus.PENDING;
            }
            blog.setStatus(targetStatus);
        }

        blog = blogRepository.save(blog);
        return mapToResponse(blog, currentUser);
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

    @Override
    public BlogCursorResponse searchBlogsCursor(String keyword, List<String> categoryNames, Long lastId, int limit, User currentUser) {
        Specification<Blog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), BlogStatus.PUBLISHED));

            if (categoryNames != null && !categoryNames.isEmpty()) {
                Predicate[] categoryPredicates = categoryNames.stream()
                        .map(name -> cb.like(cb.lower(root.get("category").get("name")), "%" + name.toLowerCase() + "%"))
                        .toArray(Predicate[]::new);
                predicates.add(cb.or(categoryPredicates));
            }

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likePattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("title")), likePattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(titlePredicate, descPredicate));
            }

            if (lastId != null) {
                predicates.add(cb.lessThan(root.get("id"), lastId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<Blog> pageResult = blogRepository.findAll(spec, pageRequest);

        List<BlogResponse> content = pageResult.getContent().stream()
                .map(b -> mapToResponse(b, currentUser))
                .collect(Collectors.toList());

        boolean hasMore = content.size() == limit && pageResult.hasNext();
        Long nextCursor = null;
        if (!content.isEmpty()) {
            nextCursor = content.get(content.size() - 1).getId();
        }

        return BlogCursorResponse.builder()
                .content(content)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    public BlogCursorResponse getMyBlogsCursor(BlogStatus status, Long lastId, int limit, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để lấy danh sách bài viết");
        }

        Specification<Blog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), currentUser.getId()));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (lastId != null) {
                predicates.add(cb.lessThan(root.get("id"), lastId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        Page<Blog> pageResult = blogRepository.findAll(spec, pageRequest);

        List<BlogResponse> content = pageResult.getContent().stream()
                .map(b -> mapToResponse(b, currentUser))
                .collect(Collectors.toList());

        boolean hasMore = content.size() == limit && pageResult.hasNext();
        Long nextCursor = null;
        if (!content.isEmpty()) {
            nextCursor = content.get(content.size() - 1).getId();
        }

        return BlogCursorResponse.builder()
                .content(content)
                .hasMore(hasMore)
                .nextCursor(nextCursor)
                .build();
    }

    @Override
    public List<String> getMyUsedThumbnails(User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để lấy danh sách ảnh bìa");
        }
        return blogRepository.findDistinctThumbnailUrlsByUserId(currentUser.getId());
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

    /**
     * Images uploaded from the editor point at file-service's temp/ area, which is cleared after a day.
     * Asks the storage module to confirm them (also for drafts) and returns temp URL -> permanent URL.
     */
    private Map<String, String> confirmTempImages(String content, String thumbnailUrl) {
        Set<String> tempUrls = new LinkedHashSet<>();
        if (content != null) {
            Matcher matcher = TEMP_IMAGE_URL.matcher(content);
            while (matcher.find()) {
                tempUrls.add(matcher.group());
            }
        }
        if (thumbnailUrl != null && TEMP_IMAGE_URL.matcher(thumbnailUrl).matches()) {
            tempUrls.add(thumbnailUrl);
        }
        if (tempUrls.isEmpty()) {
            return Map.of();
        }

        BlogImagesActivatedEvent event = new BlogImagesActivatedEvent(this, List.copyOf(tempUrls));
        eventPublisher.publishEvent(event);
        return event.getConfirmedUrls();
    }

    private static String replaceUrls(String text, Map<String, String> replacements) {
        if (text == null || replacements.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private BlogResponse mapToResponse(Blog blog, User currentUser) {
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
        int commentsCount = (int) commentRepository.countByBlogIdAndStatusNot(blog.getId(), CommentStatus.DELETED);

        boolean likedByCurrentUser = false;
        if (currentUser != null && currentUser.getId() != null) {
            likedByCurrentUser = likeRepository.findByBlogIdAndUserId(blog.getId(), currentUser.getId())
                    .map(com.blog.backend.interaction.domain.entity.Like::isLiked)
                    .orElse(false);
        }

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .status(blog.getStatus())
                .rejectionReason(blog.getRejectionReason())
                .thumbnailUrl(blog.getThumbnailUrl())
                .author(authorResponse)
                .category(categoryResponse)
                .likesCount(likesCount)
                .likedByCurrentUser(likedByCurrentUser)
                .commentsCount(commentsCount)
                .viewsCount(blog.getViewCount())
                .sharesCount(blog.getShareCount())
                .createdDate(blog.getCreatedDate())
                .modifiedDate(blog.getModifiedDate())
                .build();
    }
}
