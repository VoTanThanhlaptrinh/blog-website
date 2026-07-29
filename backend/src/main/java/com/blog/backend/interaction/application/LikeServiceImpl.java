package com.blog.be.interaction.application;

import com.blog.be.content.domain.entity.Blog;
import com.blog.be.content.domain.exception.BlogNotFoundException;
import com.blog.be.content.domain.exception.UnauthorizedBlogAccessException;
import com.blog.be.content.domain.repository.BlogRepository;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.CommentLikeResponse;
import com.blog.be.interaction.api.dto.LikeResponse;
import com.blog.be.interaction.api.dto.ToggleCommentLikeRequest;
import com.blog.be.interaction.api.dto.ToggleLikeRequest;
import com.blog.be.interaction.domain.entity.Comment;
import com.blog.be.interaction.domain.entity.CommentLike;
import com.blog.be.interaction.domain.entity.Like;
import com.blog.be.interaction.domain.enums.CommentStatus;
import com.blog.be.interaction.domain.exception.CommentAlreadyDeletedException;
import com.blog.be.interaction.domain.exception.CommentNotFoundException;
import com.blog.be.interaction.domain.repository.CommentLikeRepository;
import com.blog.be.interaction.domain.repository.CommentRepository;
import com.blog.be.interaction.domain.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service quản lý lượt Thích (Like) cho bài viết và bình luận.
 * Cơ chế State-Flip: Lần bấm đầu sẽ tạo record (liked=true), các lần bấm tiếp theo sẽ đảo ngược trạng thái (liked=true <-> false).
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final BlogRepository blogRepository;
    private final CommentRepository commentRepository;

    /**
     * Bật/Tắt thích bài viết. Nếu chưa từng thích -> Thêm mới với liked=true. Nếu đã có -> Đảo trạng thái liked.
     */
    @Override
    @Transactional
    public LikeResponse toggleLike(User currentUser, ToggleLikeRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để thực hiện thích bài viết");
        }

        Blog blog = blogRepository.findById(request.getBlogId())
                .orElseThrow(() -> new BlogNotFoundException("Không tìm thấy bài viết với ID: " + request.getBlogId()));

        Optional<Like> existingLike = likeRepository.findByBlogIdAndUserId(blog.getId(), currentUser.getId());

        boolean isLiked;
        if (existingLike.isPresent()) {
            Like like = existingLike.get();
            isLiked = !like.isLiked();
            like.setLiked(isLiked);
            likeRepository.save(like);
        } else {
            Like newLike = Like.builder()
                    .blog(blog)
                    .user(currentUser)
                    .liked(true)
                    .build();
            likeRepository.save(newLike);
            isLiked = true;
        }

        long totalLikes = likeRepository.countByBlogIdAndLikedTrue(blog.getId());

        return LikeResponse.builder()
                .blogId(blog.getId())
                .liked(isLiked)
                .totalLikes(totalLikes)
                .build();
    }

    @Override
    @Transactional
    public CommentLikeResponse toggleCommentLike(User currentUser, ToggleCommentLikeRequest request) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new UnauthorizedBlogAccessException("Vui lòng đăng nhập để thực hiện thích bình luận");
        }

        Comment comment = commentRepository.findById(request.getCommentId())
                .orElseThrow(() -> new CommentNotFoundException("Không tìm thấy bình luận với ID: " + request.getCommentId()));

        if (comment.getStatus() == CommentStatus.DELETED) {
            throw new CommentAlreadyDeletedException("Bình luận này đã bị xóa");
        }

        Optional<CommentLike> existingLike = commentLikeRepository.findByCommentIdAndUserId(comment.getId(), currentUser.getId());

        boolean isLiked;
        if (existingLike.isPresent()) {
            CommentLike commentLike = existingLike.get();
            isLiked = !commentLike.isLiked();
            commentLike.setLiked(isLiked);
            commentLikeRepository.save(commentLike);
        } else {
            CommentLike newCommentLike = CommentLike.builder()
                    .comment(comment)
                    .user(currentUser)
                    .liked(true)
                    .build();
            commentLikeRepository.save(newCommentLike);
            isLiked = true;
        }

        long totalLikes = commentLikeRepository.countByCommentIdAndLikedTrue(comment.getId());

        return CommentLikeResponse.builder()
                .commentId(comment.getId())
                .liked(isLiked)
                .totalLikes(totalLikes)
                .build();
    }
}
