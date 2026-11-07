package com.blog.backend.interaction.application.impl;

import com.blog.backend.content.api.dto.AuthorResponse;
import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.exception.BlogNotFoundException;
import com.blog.backend.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.backend.content.domain.repository.BlogRepository;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.BookmarkResponse;
import com.blog.backend.interaction.api.dto.ToggleBookmarkRequest;
import com.blog.backend.interaction.application.itf.BookmarkService;
import com.blog.backend.interaction.domain.entity.Bookmark;
import com.blog.backend.interaction.domain.enums.BookmarkStatus;
import com.blog.backend.interaction.domain.repository.BookmarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BlogRepository blogRepository;

    @Override
    @Transactional
    /**
     * Bật/Tắt lưu bài viết (Bookmark). Nếu chưa từng lưu -> Thêm mới với
     * status=ACTIVE. Nếu đã có -> Đảo trạng thái status.
     */
    public BookmarkResponse toggleBookmark(User currentUser, ToggleBookmarkRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để thực hiện lưu bài viết");
        }

        // Lấy bài viết từ DB, nếu không tồn tại -> ném ngoại lệ
        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        // Kiểm tra xem người dùng đã lưu bài viết này chưa
        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndBlogId(currentUser.getId(),
                blog.getId());

        // Xác định trạng thái mới và lưu vào DB
        boolean isBookmarked;
        Bookmark bookmark;
        // Nếu đã lưu, đảo trạng thái ACTIVE <-> REMOVED. Nếu chưa lưu, tạo mới với
        // status=ACTIVE
        if (existingBookmark.isEmpty()) {
            bookmark = Bookmark.builder()
                    .user(currentUser)
                    .blog(blog)
                    .status(BookmarkStatus.ACTIVE)
                    .build();
            isBookmarked = true;
        } else {
            bookmark = existingBookmark.get();
            bookmark.setStatus(
                    bookmark.getStatus() == BookmarkStatus.ACTIVE ? BookmarkStatus.REMOVED : BookmarkStatus.ACTIVE);
            isBookmarked = bookmark.getStatus() == BookmarkStatus.ACTIVE;
        }

        // Lưu bookmark vào DB
        bookmarkRepository.save(bookmark);

        // Trả về thông tin bookmark và trạng thái hiện tại
        return BookmarkResponse.builder()
                .blogId(blog.getId())
                .status(bookmark.getStatus())
                .bookmarked(isBookmarked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    /**
     * Lấy danh sách bài viết đã lưu (Bookmark) của người dùng hiện tại, phân trang.
     */
    public PageResponse<BlogResponse> getMyBookmarks(User currentUser, Pageable pageable) {
        // Kiểm tra người dùng đã đăng nhập chưa
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để xem bài viết đã lưu");
        }
        // Lấy danh sách bookmark của người dùng hiện tại với trạng thái ACTIVE, phân
        // trang
        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserIdAndStatus(
                currentUser.getId(), BookmarkStatus.ACTIVE, pageable);
        // Chuyển đổi danh sách bookmark sang danh sách BlogResponse
        List<BlogResponse> content = bookmarkPage.getContent().stream()
                .map(bookmark -> mapBlogToResponse(bookmark.getBlog()))
                .collect(Collectors.toList());

        // Trả về kết quả phân trang
        return PageResponse.<BlogResponse>builder()
                .content(content)
                .pageNumber(bookmarkPage.getNumber())
                .pageSize(bookmarkPage.getSize())
                .totalElements(bookmarkPage.getTotalElements())
                .totalPages(bookmarkPage.getTotalPages())
                .last(bookmarkPage.isLast())
                .build();
    }

    /**
     * Chuyển đổi đối tượng Blog sang BlogResponse.
     */
    private BlogResponse mapBlogToResponse(Blog blog) {
        if (blog == null)
            return null;
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
        int likesCount = (blog.getLikes() != null) ? blog.getLikes().size() : 0;
        int commentsCount = (blog.getComments() != null) ? blog.getComments().size() : 0;

        return BlogResponse.builder()
                .id(blog.getId())
                .title(blog.getTitle())
                .description(blog.getDescription())
                .content(blog.getContent())
                .status(blog.getStatus())
                .author(authorResponse)
                .likesCount(likesCount)
                .commentsCount(commentsCount)
                .viewsCount(blog.getViewCount())
                .sharesCount(blog.getShareCount())
                .createdDate(blog.getCreatedDate())
                .modifiedDate(blog.getModifiedDate())
                .build();
    }
}
