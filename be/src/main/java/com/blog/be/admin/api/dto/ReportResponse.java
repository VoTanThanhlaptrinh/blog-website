package com.blog.be.admin.api.dto;

import com.blog.be.admin.domain.enums.ReportStatus;
import com.blog.be.admin.domain.enums.ReportTargetType;
import com.blog.be.content.api.dto.AuthorResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private ReportTargetType targetType;
    private Long targetId;
    private String reason;
    private AuthorResponse reporter;
    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
