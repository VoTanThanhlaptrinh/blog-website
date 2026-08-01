package com.blog.backend.admin.domain.event;

import com.blog.backend.content.api.dto.BlogResponse;
import com.blog.backend.identity.domain.entity.User;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

@Getter
@Setter
public class AdminGetBlogEvent extends ApplicationEvent {
    private final Long blogId;
    private final User adminUser;
    private BlogResponse result;

    public AdminGetBlogEvent(Object source, Long blogId, User adminUser) {
        super(source);
        this.blogId = blogId;
        this.adminUser = adminUser;
    }
}
