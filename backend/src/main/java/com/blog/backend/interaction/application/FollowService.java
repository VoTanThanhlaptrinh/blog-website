package com.blog.backend.interaction.application;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.FollowStatusResponse;

public interface FollowService {
    FollowStatusResponse toggleFollow(User currentUser, Long followingId);
    FollowStatusResponse getFollowStatus(User currentUser, Long followingId);
}
