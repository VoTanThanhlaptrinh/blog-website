package com.blog.backend.admin.application.impl;

import com.blog.backend.admin.api.dto.CreateReportRequest;
import com.blog.backend.admin.api.dto.PenalizeUserRequest;
import com.blog.backend.admin.api.dto.ReportResponse;
import com.blog.backend.admin.api.dto.ResolveReportRequest;
import com.blog.backend.admin.application.AdminReportService;
import com.blog.backend.admin.domain.entity.Report;
import com.blog.backend.admin.domain.enums.PenaltyAction;
import com.blog.backend.admin.domain.enums.ReportStatus;
import com.blog.backend.admin.domain.enums.ReportTargetType;
import com.blog.backend.admin.domain.event.UserPenalizedEvent;
import com.blog.backend.admin.domain.repository.ReportRepository;
import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.identity.domain.enums.UserStatus;
import com.blog.backend.identity.domain.repository.UserRepository;
import com.blog.backend.interaction.domain.entity.Comment;
import com.blog.backend.interaction.domain.repository.CommentRepository;
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
public class AdminReportServiceImpl implements AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;
    private final ApplicationEventPublisher eventPublisher;

    private void validateAdmin(User adminUser) {
        if (adminUser == null || adminUser.getAuthorities() == null ||
                adminUser.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new UnauthorizedBlogAccessException("Chỉ có Quản trị viên mới được phép thực hiện thao tác này");
        }
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
    public PageResponse<ReportResponse> getReports(ReportTargetType targetType, ReportStatus status, Pageable pageable, User adminUser) {
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
            if (comment != null) reportedUser = comment.getCreator();
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

        report.setStatus(ReportStatus.RESOLVED_ACCEPTED);
        report.setAdminNotes(String.format("Xử phạt [%s]: %s", request.getAction(), request.getReason()));
        report = reportRepository.save(report);

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

    private ReportResponse mapToReportResponse(Report report) {
        AuthorResponse reporterResponse = null;
        if (report.getReporter() != null) {
            reporterResponse = AuthorResponse.builder()
                    .id(report.getReporter().getId())
                    .email(report.getReporter().getEmail())
                    .bio(report.getReporter().getBio())
                    .avatarUrl(report.getReporter().getAvatar() != null ? report.getReporter().getAvatar().getUrl() : null)
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
                .build();
    }
}
