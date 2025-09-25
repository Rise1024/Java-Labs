package com.javalaabs.webflux.domain.event;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户更新事件
 * 用于实时推送用户信息变更
 */
public class UserUpdateEvent {
    
    private String id;
    private String userId;
    private String eventType;
    private String fieldName;
    private Object oldValue;
    private Object newValue;
    private Instant timestamp;
    
    // 默认构造函数
    public UserUpdateEvent() {
        this.timestamp = Instant.now();
    }
    
    // 构造函数
    public UserUpdateEvent(String userId, String eventType) {
        this();
        this.userId = userId;
        this.eventType = eventType;
    }
    
    public UserUpdateEvent(String userId, String eventType, String fieldName, Object oldValue, Object newValue) {
        this(userId, eventType);
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UserUpdateEvent event;
        
        private Builder() {
            this.event = new UserUpdateEvent();
        }
        
        public Builder id(String id) {
            event.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            event.userId = userId;
            return this;
        }
        
        public Builder eventType(String eventType) {
            event.eventType = eventType;
            return this;
        }
        
        public Builder fieldName(String fieldName) {
            event.fieldName = fieldName;
            return this;
        }
        
        public Builder oldValue(Object oldValue) {
            event.oldValue = oldValue;
            return this;
        }
        
        public Builder newValue(Object newValue) {
            event.newValue = newValue;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            event.timestamp = timestamp;
            return this;
        }
        
        public UserUpdateEvent build() {
            Objects.requireNonNull(event.userId, "用户ID不能为空");
            Objects.requireNonNull(event.eventType, "事件类型不能为空");
            
            if (event.timestamp == null) {
                event.timestamp = Instant.now();
            }
            
            return event;
        }
    }
    
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public Object getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }
    
    public Object getNewValue() {
        return newValue;
    }
    
    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    // 事件类型常量
    public static final class EventTypes {
        public static final String USER_CREATED = "USER_CREATED";
        public static final String USER_UPDATED = "USER_UPDATED";
        public static final String USER_DELETED = "USER_DELETED";
        public static final String USER_ACTIVATED = "USER_ACTIVATED";
        public static final String USER_DEACTIVATED = "USER_DEACTIVATED";
        public static final String AVATAR_UPLOADED = "AVATAR_UPLOADED";
        public static final String PROFILE_VIEWED = "PROFILE_VIEWED";
        
        private EventTypes() {
            // 私有构造函数，防止实例化
        }
    }
    
    // 业务方法
    public boolean isUserUpdate() {
        return EventTypes.USER_UPDATED.equals(eventType);
    }
    
    public boolean isUserCreation() {
        return EventTypes.USER_CREATED.equals(eventType);
    }
    
    public boolean isUserDeletion() {
        return EventTypes.USER_DELETED.equals(eventType);
    }
    
    public boolean hasFieldChange() {
        return fieldName != null && !Objects.equals(oldValue, newValue);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserUpdateEvent that = (UserUpdateEvent) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserUpdateEvent{" +
               "id='" + id + '\'' +
               ", userId='" + userId + '\'' +
               ", eventType='" + eventType + '\'' +
               ", fieldName='" + fieldName + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
