package com.blog.backend.interaction.application;

import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.BookmarkResponse;
import com.blog.backend.interaction.api.dto.ToggleBookmarkRequest;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {
    BookmarkResponse toggleBookmark(User currentUser, ToggleBookmarkRequest request);
    PageResponse<BlogResponse> getMyBookmarks(User currentUser, Pageable pageable);
}
