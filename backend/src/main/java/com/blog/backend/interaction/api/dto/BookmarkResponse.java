package com.blog.backend.interaction.api.dto;

import com.blog.backend.interaction.domain.enums.BookmarkStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {
    private Long blogId;
    private BookmarkStatus status;
    private boolean bookmarked;
}
