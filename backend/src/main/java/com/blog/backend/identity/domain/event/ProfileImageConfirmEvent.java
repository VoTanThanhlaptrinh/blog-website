package com.blog.backend.identity.domain.event;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * Published synchronously when a profile is saved with a freshly uploaded avatar (temp URL);
 * the storage module confirms it and sets the permanent URL.
 */
@Getter
public class ProfileImageConfirmEvent extends ApplicationEvent {
    private final String tempUrl;

    /** Filled by the storage listener; stays null if the URL was not a temp upload. */
    @Setter
    private String permanentUrl;

    public ProfileImageConfirmEvent(Object source, String tempUrl) {
        super(source);
        this.tempUrl = tempUrl;
    }
}
