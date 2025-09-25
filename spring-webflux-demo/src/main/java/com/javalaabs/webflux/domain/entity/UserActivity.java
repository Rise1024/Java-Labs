package com.javalaabs.webflux.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户活动实体类
 * 记录用户的各种操作活动
 */
@Table("user_activities")
public class UserActivity {
    
    @Id
    private String id;
    
    @Column("user_id")
    private String userId;
    
    @Column("action")
    private String action;
    
    @Column("description")
    private String description;
    
    @Column("ip_address")
    private String ipAddress;
    
    @Column("user_agent")
    private String userAgent;
    
    @Column("timestamp")
    private Instant timestamp;
    
    // 默认构造函数
    public UserActivity() {
        this.timestamp = Instant.now();
    }
    
    // 构造函数
    public UserActivity(String userId, String action) {
        this();
        this.userId = userId;
        this.action = action;
    }
    
    public UserActivity(String userId, String action, String description) {
        this(userId, action);
        this.description = description;
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UserActivity activity;
        
        private Builder() {
            this.activity = new UserActivity();
        }
        
        public Builder id(String id) {
            activity.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            activity.userId = userId;
            return this;
        }
        
        public Builder action(String action) {
            activity.action = action;
            return this;
        }
        
        public Builder description(String description) {
            activity.description = description;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            activity.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            activity.userAgent = userAgent;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            activity.timestamp = timestamp;
            return this;
        }
        
        public UserActivity build() {
            Objects.requireNonNull(activity.userId, "用户ID不能为空");
            Objects.requireNonNull(activity.action, "操作类型不能为空");
            
            if (activity.timestamp == null) {
                activity.timestamp = Instant.now();
            }
            
            return activity;
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
    
    public String getAction() {
        return action;
    }
    
    public void setAction(String action) {
        this.action = action;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
    
    // 业务方法
    public boolean isRecentActivity() {
        return timestamp.isAfter(Instant.now().minusSeconds(300)); // 5分钟内
    }
    
    public boolean isLoginAction() {
        return "LOGIN".equals(action);
    }
    
    public boolean isLogoutAction() {
        return "LOGOUT".equals(action);
    }
    
    // 常用的活动类型常量
    public static final class ActionTypes {
        public static final String LOGIN = "LOGIN";
        public static final String LOGOUT = "LOGOUT";
        public static final String CREATE_USER = "CREATE_USER";
        public static final String UPDATE_USER = "UPDATE_USER";
        public static final String DELETE_USER = "DELETE_USER";
        public static final String UPLOAD_AVATAR = "UPLOAD_AVATAR";
        public static final String VIEW_PROFILE = "VIEW_PROFILE";
        public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
        
        private ActionTypes() {
            // 私有构造函数，防止实例化
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserActivity that = (UserActivity) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserActivity{" +
               "id='" + id + '\'' +
               ", userId='" + userId + '\'' +
               ", action='" + action + '\'' +
               ", description='" + description + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
