package com.blog.backend.content.api.dto;

import com.blog.backend.content.domain.enums.BlogStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String description;
    private String content;
    private BlogStatus status;
    private String rejectionReason;
    private AuthorResponse author;
    private CategoryResponse category;
    private int likesCount;
    private int commentsCount;
    private int viewsCount;
    private int sharesCount;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
