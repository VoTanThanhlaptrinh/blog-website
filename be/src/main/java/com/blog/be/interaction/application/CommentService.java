package com.blog.be.interaction.application;

import com.blog.be.content.api.dto.PageResponse;
import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.CommentResponse;
import com.blog.be.interaction.api.dto.CreateCommentRequest;
import com.blog.be.interaction.api.dto.UpdateCommentRequest;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponse createComment(User currentUser, CreateCommentRequest request);
    CommentResponse updateComment(Long commentId, User currentUser, UpdateCommentRequest request);
    void deleteComment(Long commentId, User currentUser);
    PageResponse<CommentResponse> getCommentsByBlogId(Long blogId, Pageable pageable, User currentUser);
}
