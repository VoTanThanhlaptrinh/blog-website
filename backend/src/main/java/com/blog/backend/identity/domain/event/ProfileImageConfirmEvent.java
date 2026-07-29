package com.blog.backend.identity.domain.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProfileImageConfirmEvent extends ApplicationEvent {
    private final Long imageId;
    private final String tempUrl;

    public ProfileImageConfirmEvent(Object source, Long imageId, String tempUrl) {
        super(source);
        this.imageId = imageId;
        this.tempUrl = tempUrl;
    }
}
