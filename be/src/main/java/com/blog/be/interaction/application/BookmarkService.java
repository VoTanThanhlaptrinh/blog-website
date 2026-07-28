package com.blog.be.interaction.application;

import com.blog.be.content.api.dto.BlogResponse;
import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.BookmarkResponse;
import com.blog.be.interaction.api.dto.ToggleBookmarkRequest;
import org.springframework.data.domain.Pageable;

public interface BookmarkService {
    BookmarkResponse toggleBookmark(User currentUser, ToggleBookmarkRequest request);
    PageResponse<BlogResponse> getMyBookmarks(User currentUser, Pageable pageable);
}
