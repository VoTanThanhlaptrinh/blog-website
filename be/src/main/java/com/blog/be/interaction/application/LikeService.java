package com.blog.be.interaction.application;

import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.CommentLikeResponse;
import com.blog.be.interaction.api.dto.LikeResponse;
import com.blog.be.interaction.api.dto.ToggleCommentLikeRequest;
import com.blog.be.interaction.api.dto.ToggleLikeRequest;

public interface LikeService {
    LikeResponse toggleLike(User currentUser, ToggleLikeRequest request);
    CommentLikeResponse toggleCommentLike(User currentUser, ToggleCommentLikeRequest request);
}
