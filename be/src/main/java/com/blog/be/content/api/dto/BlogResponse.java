package com.blog.be.content.api.dto;

import com.blog.be.content.domain.enums.BlogStatus;
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
    private AuthorResponse author;
    private int likesCount;
    private int commentsCount;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
