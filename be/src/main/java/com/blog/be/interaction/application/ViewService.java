package com.blog.be.interaction.application;

import com.blog.be.identity.domain.entity.User;
import com.blog.be.interaction.api.dto.RecordViewRequest;
import com.blog.be.interaction.api.dto.ViewResponse;

public interface ViewService {
    ViewResponse recordView(User currentUser, String ipAddress, RecordViewRequest request);
}
