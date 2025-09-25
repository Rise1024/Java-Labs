package com.javalaabs.webflux.domain.event;

import org.springframework.context.ApplicationEvent;

import java.time.Instant;

/**
 * 用户创建事件
 * 当用户被成功创建时发布此事件
 */
public class UserCreatedEvent extends ApplicationEvent {
    
    private final String userId;
    private final String userEmail;
    private final Instant timestamp;
    
    public UserCreatedEvent(Object source, String userId, String userEmail) {
        super(source);
        this.userId = userId;
        this.userEmail = userEmail;
        this.timestamp = Instant.now();
    }
    
    public UserCreatedEvent(String userId) {
        this(UserCreatedEvent.class, userId, null);
    }
    
    public UserCreatedEvent(String userId, String userEmail) {
        this(UserCreatedEvent.class, userId, userEmail);
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public Instant getEventTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "UserCreatedEvent{" +
               "userId='" + userId + '\'' +
               ", userEmail='" + userEmail + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
