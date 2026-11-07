package com.blog.backend.admin.application.impl;

import com.blog.backend.admin.api.dto.AdminDashboardSummaryResponse;
import com.blog.backend.admin.api.dto.DailyGrowthResponse;
import com.blog.backend.admin.application.AdminDashboardService;
import com.blog.backend.admin.domain.enums.ReportStatus;
import com.blog.backend.admin.domain.repository.ReportRepository;
import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.CategoryResponse;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.interaction.domain.enums.CommentStatus;
import com.blog.backend.interaction.domain.repository.CommentRepository;
import com.blog.backend.interaction.domain.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final BlogRepository blogRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    private void validateAdmin(User adminUser) {
        if (adminUser == null || adminUser.getAuthorities() == null ||
                adminUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedBlogAccessException("Chỉ có Quản trị viên mới được phép thực hiện thao tác này");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardSummaryResponse getDashboardSummary(User adminUser) {
        validateAdmin(adminUser);

        long totalUsers = userRepository.count();
        long totalBlogs = blogRepository.countByStatus(BlogStatus.PUBLISHED);
        long totalComments = commentRepository.count();
        long pendingBlogsCount = blogRepository.countByStatus(BlogStatus.PENDING);
        long pendingReportsCount = reportRepository.countByStatus(ReportStatus.PENDING);

        return AdminDashboardSummaryResponse.builder()
                .totalUsers(totalUsers)
                .totalBlogs(totalBlogs)
                .totalComments(totalComments)
                .pendingBlogsCount(pendingBlogsCount)
                .pendingReportsCount(pendingReportsCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailyGrowthResponse> getGrowthStats(int days, User adminUser) {
        validateAdmin(adminUser);

        List<DailyGrowthResponse> list = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            long newUsers = userRepository.countByCreatedDateBetween(startOfDay, endOfDay);
            long newBlogs = blogRepository.countByCreatedDateBetweenAndStatus(startOfDay, endOfDay, BlogStatus.PUBLISHED);

            list.add(DailyGrowthResponse.builder()
                    .date(date)
                    .newUsersCount(newUsers)
                    .newBlogsCount(newBlogs)
                    .build());
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponse> getTopBlogs(int limit, User adminUser) {
        validateAdmin(adminUser);

        List<Blog> blogs = blogRepository.findByStatusOrderByViewCountDesc(BlogStatus.PUBLISHED, PageRequest.of(0, limit)).getContent();
        return blogs.stream()
                .map(this::mapToBlogResponse)
                .collect(Collectors.toList());
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
        int commentsCount = (int) commentRepository.countByBlogIdAndStatusNot(blog.getId(), CommentStatus.DELETED);

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
