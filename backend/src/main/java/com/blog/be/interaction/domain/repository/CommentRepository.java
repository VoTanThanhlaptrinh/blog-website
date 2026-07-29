package com.blog.be.interaction.domain.repository;

import com.blog.be.interaction.domain.entity.Comment;
import com.blog.be.interaction.domain.enums.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByBlogIdAndParentIsNullAndStatusNot(Long blogId, CommentStatus status, Pageable pageable);
    List<Comment> findByParentIdAndStatusNot(Long parentId, CommentStatus status);
    long countByBlogIdAndStatusNot(Long blogId, CommentStatus status);
}
