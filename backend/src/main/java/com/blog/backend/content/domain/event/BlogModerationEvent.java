package com.blog.backend.content.domain.event;

import com.blog.backend.content.domain.entity.Blog;
import com.blog.backend.content.domain.enums.BlogStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BlogModerationEvent extends ApplicationEvent {
    private final Blog blog;
    private final BlogStatus newStatus;
    private final String rejectionReason;

    public BlogModerationEvent(Object source, Blog blog, BlogStatus newStatus, String rejectionReason) {
        super(source);
        this.blog = blog;
        this.newStatus = newStatus;
        this.rejectionReason = rejectionReason;
    }
}
