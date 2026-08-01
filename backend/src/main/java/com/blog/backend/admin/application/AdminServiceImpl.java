package com.blog.backend.admin.application;

import com.blog.backend.admin.api.dto.*;
import com.blog.backend.admin.domain.entity.Report;
import com.blog.backend.admin.domain.enums.PenaltyAction;
import com.blog.backend.admin.domain.enums.ReportStatus;
import com.blog.backend.admin.domain.enums.ReportTargetType;
import com.blog.backend.admin.domain.event.UserPenalizedEvent;
import com.blog.backend.interaction.domain.entity.Comment;
import com.blog.backend.admin.domain.repository.ReportRepository;
import com.blog.backend.admin.domain.repository.SystemSettingRepository;
import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import com.blog.backend.content.domain.event.BlogModerationEvent;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.identity.domain.repository.RoleRepository;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.interaction.domain.repository.CommentRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.blog.backend.interaction.domain.enums.CommentStatus;
import com.blog.backend.interaction.domain.repository.LikeRepository;

/**
 * Service xử lý toàn bộ các nghiệp vụ dành riêng cho Quản trị viên (Admin):
 * 1. Kiểm duyệt bài viết (Tiền kiểm PENDING -> PUBLISHED / REJECTED).
 * 2. Quản lý và xử lý Báo cáo vi phạm (Report).
 * 3. Thống kê số liệu Analytics cho Dashboard.
 */

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final BlogRepository blogRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SystemSettingRepository systemSettingRepository;
    private final RoleRepository roleRepository;

    /**
     * Lấy danh sách các bài viết cần duyệt (mặc định lấy trạng thái PENDING).
     */
    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogResponse> getBlogsForModeration(BlogStatus status, String keyword, Pageable pageable,
            User adminUser) {
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

    /**
     * Phê duyệt bài viết: Chuyển trạng thái sang PUBLISHED và kích hoạt sự kiện gửi
     * thông báo (In-App + Email) cho Tác giả.
     */
    @Override
    @Transactional
    public BlogResponse approveBlog(Long blogId, User adminUser) {
        validateAdmin(adminUser);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + blogId));

        blog.setStatus(BlogStatus.PUBLISHED);
        blog.setRejectionReason(null);
        blog = blogRepository.save(blog);

        // Phát sự kiện bất đồng bộ để thông báo cho tác giả
        eventPublisher.publishEvent(new BlogModerationEvent(this, blog, BlogStatus.PUBLISHED, null));

        return mapToBlogResponse(blog);
    }

    /**
     * Từ chối xuất bản bài viết: Đặt lý do từ chối, chuyển trạng thái sang REJECTED
     * và gửi thông báo cho Tác giả.
     */
    @Override
    @Transactional
    public BlogResponse rejectBlog(Long blogId, RejectBlogRequest request, User adminUser) {
        validateAdmin(adminUser);
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + blogId));

        blog.setStatus(BlogStatus.REJECTED);
        blog.setRejectionReason(request.getReason());
        blog = blogRepository.save(blog);

        // Phát sự kiện bất đồng bộ gửi lý do từ chối cho tác giả
        eventPublisher.publishEvent(new BlogModerationEvent(this, blog, BlogStatus.REJECTED, request.getReason()));

        return mapToBlogResponse(blog);
    }

    @Override
    @Transactional
    public ReportResponse createReport(User reporter, CreateReportRequest request) {
        if (reporter == null || reporter.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để gửi báo cáo");
        }

        Report report = Report.builder()
                .targetType(request.getTargetType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .reporter(reporter)
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        return mapToReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable,
            User adminUser) {
        validateAdmin(adminUser);

        Specification<Report> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (targetType != null) {
                predicates.add(cb.equal(root.get("targetType"), targetType));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Report> pageResult = reportRepository.findAll(spec, pageable);
        List<ReportResponse> content = pageResult.getContent().stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());

        return PageResponse.<ReportResponse>builder()
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
    public ReportResponse resolveReport(Long reportId, ResolveReportRequest request, User adminUser) {
        validateAdmin(adminUser);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo với ID: " + reportId));

        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report = reportRepository.save(report);

        return mapToReportResponse(report);
    }

    @Override
    @Transactional
    public ReportResponse penalizeUser(Long reportId, PenalizeUserRequest request, User adminUser) {
        validateAdmin(adminUser);
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy báo cáo với ID: " + reportId));

        User reportedUser = null;
        if (report.getTargetType() == ReportTargetType.USER) {
            reportedUser = userRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng bị báo cáo"));
        } else if (report.getTargetType() == ReportTargetType.BLOG) {
            Blog blog = blogRepository.findById(report.getTargetId()).orElse(null);
            if (blog != null) reportedUser = blog.getUser();
        } else if (report.getTargetType() == ReportTargetType.COMMENT) {
            Comment comment = commentRepository.findById(report.getTargetId()).orElse(null);
            if (comment != null) reportedUser = comment.getUser();
        }

        if (reportedUser == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng liên quan đến báo cáo này");
        }

        if (request.getAction() == PenaltyAction.LOCK) {
            reportedUser.setStatus(UserStatus.BANNED);
        } else if (request.getAction() == PenaltyAction.WARN) {
            int newCount = reportedUser.getWarningCount() + 1;
            reportedUser.setWarningCount(newCount);
            if (newCount >= 3) {
                reportedUser.setStatus(UserStatus.BANNED);
            }
        }

        userRepository.save(reportedUser);

        report.setStatus(ReportStatus.RESOLVED);
        report.setAdminNotes(String.format("Xử phạt [%s]: %s", request.getAction(), request.getReason()));
        report = reportRepository.save(report);

        // Phát sự kiện để gửi thông báo In-app và Email
        eventPublisher.publishEvent(new UserPenalizedEvent(
                this,
                reportedUser,
                request.getAction(),
                request.getReason(),
                reportedUser.getWarningCount()
        ));

        return mapToReportResponse(report);
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
        if (days <= 0)
            days = 30;

        List<DailyGrowthResponse> growthList = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            long newUsers = userRepository.countByCreatedDateBetween(startOfDay, endOfDay);
            long newBlogs = blogRepository.countByCreatedDateBetween(startOfDay, endOfDay);

            growthList.add(DailyGrowthResponse.builder()
                    .date(date)
                    .newUsersCount(newUsers)
                    .newBlogsCount(newBlogs)
                    .build());
        }

        return growthList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogResponse> getTopBlogs(int limit, User adminUser) {
        validateAdmin(adminUser);
        if (limit <= 0)
            limit = 10;

        Pageable pageable = PageRequest.of(0, limit);
        Page<Blog> topPage = blogRepository.findByStatusOrderByViewCountDesc(BlogStatus.PUBLISHED, pageable);

        return topPage.getContent().stream()
                .map(this::mapToBlogResponse)
                .collect(Collectors.toList());
    }

    private void validateAdmin(User user) {
        if (user == null || user.getAuthorities() == null) {
            throw new UnauthorizedBlogAccessException("Bạn không có quyền quản trị");
        }
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "ADMIN".equals(auth.getAuthority()));
        if (!isAdmin) {
            throw new UnauthorizedBlogAccessException("Truy cập bị từ chối: Yêu cầu quyền ADMIN");
        }
    }

    private BlogResponse mapToBlogResponse(Blog blog) {
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
        int likesCount = (int) likeRepository.countByBlogIdAndLikedTrue(blog.getId());
        int commentsCount = (int) commentRepository.countByBlogIdAndStatusNot(blog.getId(), CommentStatus.DELETED);

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .status(blog.getStatus())
                .rejectionReason(blog.getRejectionReason())
                .author(authorResponse)
                .likesCount(likesCount)
                .commentsCount(commentsCount)
                .viewsCount(blog.getViewCount())
                .sharesCount(blog.getShareCount())
                .createdDate(blog.getCreatedDate())
                .modifiedDate(blog.getModifiedDate())
                .build();
    }

    private ReportResponse mapToReportResponse(Report report) {
        AuthorResponse reporterResponse = null;
        if (report.getReporter() != null) {
            User u = report.getReporter();
            reporterResponse = AuthorResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .avatarUrl(u.getAvatar() != null ? u.getAvatar().getUrl() : null)
                    .bio(u.getBio())
                    .build();
        }

        return ReportResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .reporter(reporterResponse)
                .status(report.getStatus())
                .adminNotes(report.getAdminNotes())
                .createdDate(report.getCreatedDate())
                .modifiedDate(report.getModifiedDate())
                .build();
    }

    // ================= USER MANAGEMENT =================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminUserResponse> getUsers(String role, UserStatus status, String keyword, Pageable pageable, User adminUser) {
        validateAdmin(adminUser);

        Specification<User> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate p1 = cb.like(cb.lower(root.get("email")), pattern);
                Predicate p2 = cb.like(cb.lower(root.get("phone")), pattern);
                predicates.add(cb.or(p1, p2));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<User> pageResult = userRepository.findAll(spec, pageable);
        List<AdminUserResponse> content = pageResult.getContent().stream()
                .map(this::mapToAdminUserResponse)
                .collect(Collectors.toList());

        return PageResponse.<AdminUserResponse>builder()
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
    public AdminUserResponse updateUserStatus(Long userId, UpdateUserStatusRequest request, User adminUser) {
        validateAdmin(adminUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        user.setStatus(request.getStatus());
        if (request.getStatus() == com.blog.backend.identity.domain.enums.UserStatus.ACTIVE) {
            user.setEnabled(true);
        } else if (request.getStatus() == com.blog.backend.identity.domain.enums.UserStatus.BANNED) {
            user.setEnabled(false);
        }
        user = userRepository.save(user);

        return mapToAdminUserResponse(user);
    }

    @Override
    @Transactional
    public AdminUserResponse updateUserRole(Long userId, UpdateUserRoleRequest request, User adminUser) {
        validateAdmin(adminUser);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        String roleName = request.getRole();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName.toUpperCase();
        }

        com.blog.backend.identity.domain.entity.Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy role: " + request.getRole()));

        if (user.getUserRoles() != null) {
            user.getUserRoles().clear();
        } else {
            user.setUserRoles(new ArrayList<>());
        }

        user.getUserRoles().add(com.blog.backend.identity.domain.entity.UserRole.builder()
                .user(user)
                .role(role)
                .status(com.blog.backend.identity.domain.enums.UserRoleStatus.ACTIVE)
                .build());

        user = userRepository.save(user);
        return mapToAdminUserResponse(user);
    }

    private AdminUserResponse mapToAdminUserResponse(User user) {
        List<String> roles = user.getUserRoles() == null ? List.of() :
                user.getUserRoles().stream()
                        .filter(ur -> ur.getStatus() == com.blog.backend.identity.domain.enums.UserRoleStatus.ACTIVE)
                        .map(ur -> ur.getRole().getName().replace("ROLE_", ""))
                        .collect(Collectors.toList());

        long postsCount = blogRepository.countByUserIdAndStatusNot(user.getId(), BlogStatus.DRAFT);

        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .bio(user.getBio())
                .avatarUrl(user.getAvatar() != null ? user.getAvatar().getUrl() : null)
                .status(user.getStatus())
                .roles(roles)
                .postsCount(postsCount)
                .createdDate(user.getCreatedDate())
                .build();
    }

    // ================= SYSTEM SETTINGS =================

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, String> getSystemSettings(User adminUser) {
        validateAdmin(adminUser);
        List<com.blog.backend.admin.domain.entity.SystemSetting> settingsList = systemSettingRepository.findAll();
        java.util.Map<String, String> resultMap = new java.util.HashMap<>();

        // Default values
        resultMap.put("siteName", "B-BlogHub");
        resultMap.put("siteDescription", "Nền tảng chia sẻ bài viết kỹ thuật & công nghệ");
        resultMap.put("maintenanceMode", "false");
        resultMap.put("maxUploadSizeMb", "10");

        for (com.blog.backend.admin.domain.entity.SystemSetting setting : settingsList) {
            resultMap.put(setting.getKey(), setting.getValue());
        }

        return resultMap;
    }

    @Override
    @Transactional
    public java.util.Map<String, String> updateSystemSettings(java.util.Map<String, String> settings, User adminUser) {
        validateAdmin(adminUser);
        for (java.util.Map.Entry<String, String> entry : settings.entrySet()) {
            com.blog.backend.admin.domain.entity.SystemSetting setting = systemSettingRepository.findByKey(entry.getKey())
                    .orElse(com.blog.backend.admin.domain.entity.SystemSetting.builder()
                            .key(entry.getKey())
                            .build());
            setting.setValue(entry.getValue());
            systemSettingRepository.save(setting);
        }
        return getSystemSettings(adminUser);
    }

    // ================= CSV EXPORTS =================

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUsersCsv(String role, com.blog.backend.identity.domain.enums.UserStatus status, String keyword, User adminUser) {
        validateAdmin(adminUser);
        List<User> users = userRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Email,Phone,Status,CreatedDate\n");
        for (User u : users) {
            sb.append(u.getId()).append(",")
              .append(u.getEmail() != null ? u.getEmail() : "").append(",")
              .append(u.getPhone() != null ? u.getPhone() : "").append(",")
              .append(u.getStatus() != null ? u.getStatus() : "").append(",")
              .append(u.getCreatedDate() != null ? u.getCreatedDate() : "").append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
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

    @Override
    @Transactional(readOnly = true)
    public byte[] exportReportsCsv(ReportTargetType targetType, ReportStatus status, User adminUser) {
        validateAdmin(adminUser);
        List<Report> reports = reportRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,TargetType,TargetID,Reason,ReporterEmail,Status,CreatedDate\n");
        for (Report r : reports) {
            sb.append(r.getId()).append(",")
              .append(r.getTargetType() != null ? r.getTargetType() : "").append(",")
              .append(r.getTargetId() != null ? r.getTargetId() : "").append(",")
              .append("\"").append(r.getReason() != null ? r.getReason().replace("\"", "\"\"") : "").append("\",")
              .append(r.getReporter() != null ? r.getReporter().getEmail() : "").append(",")
              .append(r.getStatus() != null ? r.getStatus() : "").append(",")
              .append(r.getCreatedDate() != null ? r.getCreatedDate() : "").append("\n");
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }
}
