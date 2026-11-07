package com.blog.backend.interaction.api;

import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.BookmarkResponse;
import com.blog.backend.interaction.api.dto.ToggleBookmarkRequest;
import com.blog.backend.interaction.application.itf.BookmarkService;
import com.blog.backend.notification.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<BookmarkResponse>> toggleBookmark(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ToggleBookmarkRequest request,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(null,
                    Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage(), 400));
        }

        BookmarkResponse res = bookmarkService.toggleBookmark(currentUser, request);
        return ResponseEntity.ok(new ApiResponse<>(res, "Thao tác lưu bài viết thành công", 200));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PageResponse<BlogResponse>>> getMyBookmarks(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {

        PageResponse<BlogResponse> res = bookmarkService.getMyBookmarks(currentUser, pageable);
        return ResponseEntity.ok(new ApiResponse<>(res, "Lấy danh sách bài viết đã lưu thành công", 200));
    }
}
