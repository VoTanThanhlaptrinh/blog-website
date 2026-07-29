package com.blog.backend.interaction.domain.repository;

import com.blog.backend.interaction.domain.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.blog.backend.interaction.domain.enums.BookmarkStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByUserIdAndBlogId(Long userId, Long blogId);

    Page<Bookmark> findByUserIdAndStatus(Long userId, BookmarkStatus status, Pageable pageable);
}
