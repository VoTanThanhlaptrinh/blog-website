package com.blog.backend.interaction.application.itf;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CommentLikeResponse;
import com.blog.backend.interaction.api.dto.LikeResponse;
import com.blog.backend.interaction.api.dto.ToggleCommentLikeRequest;
import com.blog.backend.interaction.api.dto.ToggleLikeRequest;

public interface LikeService {
    LikeResponse toggleLike(User currentUser, ToggleLikeRequest request);
    CommentLikeResponse toggleCommentLike(User currentUser, ToggleCommentLikeRequest request);
}
