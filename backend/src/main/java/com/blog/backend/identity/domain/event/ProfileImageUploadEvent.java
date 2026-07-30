package com.blog.backend.identity.domain.event;

import com.blog.backend.storage.api.dto.UploadPostResponse;
import com.blog.backend.storage.api.dto.UploadUrlRequest;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProfileImageUploadEvent extends ApplicationEvent {
    private final UploadUrlRequest request;
    
    @Setter
    private UploadPostResponse response;

    public ProfileImageUploadEvent(Object source, UploadUrlRequest request) {
        super(source);
        this.request = request;
    }
}
