package com.blog.backend.interaction.api.dto;

import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.interaction.domain.enums.CommentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorResponse author;
    private Long blogId;
    private Long parentId;
    private CommentStatus status;
    private long likeCount;
    private boolean likedByCurrentUser;
    private List<CommentResponse> replies;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
