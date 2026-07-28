package com.blog.be.interaction.application;

import com.blog.be.content.api.dto.AuthorResponse;
import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.exception.BlogNotFoundException;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.CommentResponse;
import com.blog.be.interaction.api.dto.CreateCommentRequest;
import com.blog.be.interaction.api.dto.UpdateCommentRequest;
import com.blog.be.interaction.domain.entity.Comment;
import com.blog.be.interaction.domain.enums.CommentStatus;
import com.blog.be.interaction.domain.exception.*;
import com.blog.be.interaction.domain.repository.CommentLikeRepository;
import com.blog.be.interaction.domain.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BlogRepository blogRepository;

    @Override
    @Transactional
    public CommentResponse createComment(User currentUser, CreateCommentRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedCommentAccessException("Vui lòng đăng nhập để bình luận");
        }

        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        Comment parentComment = null;
        if (request.getParentId() != null) {
            parentComment = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ParentCommentNotFoundException("Không tìm thấy bình luận cha với ID: " + request.getParentId()));

            if (parentComment.getStatus() == CommentStatus.DELETED) {
                throw new CommentAlreadyDeletedException("Không thể trả lời bình luận đã bị xóa");
            }

            if (parentComment.getParent() != null) {
                throw new CannotReplyToReplyException("Hệ thống chỉ hỗ trợ trả lời bình luận 1 cấp");
            }
        }

        Comment comment = Comment.builder()
                .content(request.getContent())
                .blog(blog)
                .creator(currentUser)
                .parent(parentComment)
                .status(CommentStatus.ACTIVE)
                .build();

        comment = commentRepository.save(comment);
        return mapToResponse(comment, currentUser, true);
    }

    @Override
    @Transactional
    public CommentResponse updateComment(Long commentId, User currentUser, UpdateCommentRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedCommentAccessException("Vui lòng đăng nhập để chỉnh sửa bình luận");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new CommentAlreadyDeletedException("Bình luận đã bị xóa và không thể chỉnh sửa");
        }

        if (comment.getCreator() == null || !comment.getCreator().getId().equals(currentUser.getId())) {
            throw new UnauthorizedCommentAccessException("Bạn không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(request.getContent());
        comment = commentRepository.save(comment);

        return mapToResponse(comment, currentUser, true);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId, User currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedCommentAccessException("Vui lòng đăng nhập để xóa bình luận");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Không tìm thấy bình luận với ID: " + commentId));

        if (comment.getStatus() == CommentStatus.DELETED) {
            return;
        }

        boolean isCommentCreator = comment.getCreator() != null && comment.getCreator().getId().equals(currentUser.getId());
        boolean isBlogAuthor = comment.getBlog() != null && comment.getBlog().getUser() != null && comment.getBlog().getUser().getId().equals(currentUser.getId());
        boolean isAdmin = isAdmin(currentUser);

        if (!isCommentCreator && !isBlogAuthor && !isAdmin) {
            throw new UnauthorizedCommentAccessException("Bạn không có quyền xóa bình luận này");
        }

        comment.setStatus(CommentStatus.DELETED);
        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable, User currentUser) {
        if (!blogRepository.existsById(blogId)) {
            throw new BlogNotFoundException("Không tìm thấy bài viết với ID: " + blogId);
        }

        Page<Comment> parentCommentsPage = commentRepository.findByBlogIdAndParentIsNullAndStatusNot(
                blogId, CommentStatus.DELETED, pageable);

        List<CommentResponse> content = parentCommentsPage.getContent().stream()
                .map(comment -> mapToResponse(comment, currentUser, true))
                .collect(Collectors.toList());

        return PageResponse.<CommentResponse>builder()
                .content(content)
                .pageNumber(parentCommentsPage.getNumber())
                .pageSize(parentCommentsPage.getSize())
                .totalElements(parentCommentsPage.getTotalElements())
                .totalPages(parentCommentsPage.getTotalPages())
                .last(parentCommentsPage.isLast())
                .build();
    }

    private CommentResponse mapToResponse(Comment comment, User currentUser, boolean includeReplies) {
        AuthorResponse authorResponse = null;
        if (comment.getCreator() != null) {
            User u = comment.getCreator();
            authorResponse = AuthorResponse.builder()
                    .id(u.getId())
                    .email(u.getEmail())
                    .avatarUrl(u.getAvatar() != null ? u.getAvatar().getUrl() : null)
                    .bio(u.getBio())
                    .build();
        }

        long likeCount = commentLikeRepository.countByCommentIdAndLikedTrue(comment.getId());
        boolean likedByCurrentUser = currentUser != null && currentUser.getId() != null
                && commentLikeRepository.existsByCommentIdAndUserIdAndLikedTrue(comment.getId(), currentUser.getId());

        List<CommentResponse> replies = new ArrayList<>();
        if (includeReplies) {
            List<Comment> rawReplies = commentRepository.findByParentIdAndStatusNot(comment.getId(), CommentStatus.DELETED);
            replies = rawReplies.stream()
                    .sorted(Comparator.comparing(Comment::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(reply -> mapToResponse(reply, currentUser, false))
                    .collect(Collectors.toList());
        }

        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .author(authorResponse)
                .blogId(comment.getBlog() != null ? comment.getBlog().getId() : null)
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .status(comment.getStatus())
                .likeCount(likeCount)
                .likedByCurrentUser(likedByCurrentUser)
                .replies(replies)
                .createdDate(comment.getCreatedDate())
                .lastModifiedDate(comment.getLastModifiedDate())
                .build();
    }

    private boolean isAdmin(User user) {
        if (user == null || user.getAuthorities() == null) return false;
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()) || "ADMIN".equals(auth.getAuthority()));
    }
}
