package com.blog.be.interaction.domain.repository;

import com.blog.be.interaction.domain.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByBlogIdAndUserId(Long blogId, Long userId);
    long countByBlogIdAndLikedTrue(Long blogId);
    long countByLikedTrue();
    boolean existsByBlogIdAndUserIdAndLikedTrue(Long blogId, Long userId);
}
