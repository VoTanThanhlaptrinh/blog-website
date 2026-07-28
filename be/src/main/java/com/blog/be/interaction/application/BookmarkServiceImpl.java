package com.blog.be.interaction.application;

import com.blog.be.content.api.dto.AuthorResponse;
import com.blog.be.content.api.dto.BlogResponse;
import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.exception.BlogNotFoundException;
import com.blog.be.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.BookmarkResponse;
import com.blog.be.interaction.api.dto.ToggleBookmarkRequest;
import com.blog.be.interaction.domain.entity.Bookmark;
import com.blog.be.interaction.domain.enums.BookmarkStatus;
import com.blog.be.interaction.domain.repository.BookmarkRepository;
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
    public BookmarkResponse toggleBookmark(User currentUser, ToggleBookmarkRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để thực hiện lưu bài viết");
        }

        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserIdAndBlogId(currentUser.getId(), blog.getId());

        BookmarkStatus newStatus;
        boolean isBookmarked;

        if (existingBookmark.isPresent()) {
            Bookmark bookmark = existingBookmark.get();
            if (bookmark.getStatus() == BookmarkStatus.ACTIVE) {
                newStatus = BookmarkStatus.REMOVED;
                isBookmarked = false;
            } else {
                newStatus = BookmarkStatus.ACTIVE;
                isBookmarked = true;
            }
            bookmark.setStatus(newStatus);
            bookmarkRepository.save(bookmark);
        } else {
            Bookmark newBookmark = Bookmark.builder()
                    .user(currentUser)
                    .blog(blog)
                    .status(BookmarkStatus.ACTIVE)
                    .build();
            bookmarkRepository.save(newBookmark);
            newStatus = BookmarkStatus.ACTIVE;
            isBookmarked = true;
        }

        return BookmarkResponse.builder()
                .blogId(blog.getId())
                .status(newStatus)
                .bookmarked(isBookmarked)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogResponse> getMyBookmarks(User currentUser, Pageable pageable) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để xem bài viết đã lưu");
        }

        Page<Bookmark> bookmarkPage = bookmarkRepository.findByUserIdAndStatus(
                currentUser.getId(), BookmarkStatus.ACTIVE, pageable);

        List<BlogResponse> content = bookmarkPage.getContent().stream()
                .map(bookmark -> mapBlogToResponse(bookmark.getBlog()))
                .collect(Collectors.toList());

        return PageResponse.<BlogResponse>builder()
                .content(content)
                .pageNumber(bookmarkPage.getNumber())
                .pageSize(bookmarkPage.getSize())
                .totalElements(bookmarkPage.getTotalElements())
                .totalPages(bookmarkPage.getTotalPages())
                .last(bookmarkPage.isLast())
                .build();
    }

    private BlogResponse mapBlogToResponse(Blog blog) {
        if (blog == null) return null;
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
