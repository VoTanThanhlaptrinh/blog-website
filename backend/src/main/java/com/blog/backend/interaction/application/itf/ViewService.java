package com.blog.backend.interaction.application.itf;

import com.blog.backend.identity.domain.entity.User;
import com.blog.backend.interaction.api.dto.RecordViewRequest;
import com.blog.backend.interaction.api.dto.ViewResponse;

public interface ViewService {
    ViewResponse recordView(User currentUser, String ipAddress, RecordViewRequest request);
}
