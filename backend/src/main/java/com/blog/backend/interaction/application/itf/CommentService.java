package com.blog.backend.interaction.application.itf;

import com.blog.backend.content.api.dto.PageResponse;
import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CommentResponse;
import com.blog.backend.interaction.api.dto.CreateCommentRequest;
import com.blog.backend.interaction.api.dto.UpdateCommentRequest;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse createComment(User currentUser, CreateCommentRequest request);
    CommentResponse updateComment(Long commentId, User currentUser, UpdateCommentRequest request);
    void deleteComment(Long commentId, User currentUser);
    PageResponse<CommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable, User currentUser);
}
