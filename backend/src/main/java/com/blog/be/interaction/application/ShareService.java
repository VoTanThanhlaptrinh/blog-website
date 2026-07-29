package com.blog.be.interaction.application;

import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.CreateShareRequest;
import com.blog.be.interaction.api.dto.ShareResponse;

public interface ShareService {
    ShareResponse createShare(User currentUser, CreateShareRequest request);
}
