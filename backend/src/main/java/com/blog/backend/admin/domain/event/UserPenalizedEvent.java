package com.blog.backend.admin.domain.event;

import com.blog.backend.admin.domain.enums.PenaltyAction;
import com.blog.backend.identity.domain.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserPenalizedEvent extends ApplicationEvent {
    private final User user;
    private final PenaltyAction action;
    private final String reason;
    private final int currentWarningCount;

    public UserPenalizedEvent(Object source, User user, PenaltyAction action, String reason, int currentWarningCount) {
        super(source);
        this.user = user;
        this.action = action;
        this.reason = reason;
        this.currentWarningCount = currentWarningCount;
    }
}
