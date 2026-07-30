package com.blog.backend.interaction.domain.repository;

import com.blog.backend.interaction.domain.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);
    long countByCommentIdAndLikedTrue(Long commentId);
    boolean existsByCommentIdAndUserIdAndLikedTrue(Long commentId, Long userId);
}
