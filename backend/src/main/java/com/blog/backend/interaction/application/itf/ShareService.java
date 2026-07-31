package com.blog.backend.interaction.application.itf;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.CreateShareRequest;
import com.blog.backend.interaction.api.dto.ShareResponse;

public interface ShareService {
    ShareResponse createShare(User currentUser, CreateShareRequest request);
}
