package com.javalaabs.webflux.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户活动数据传输对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserActivityDTO {
    
    private String id;
    private String userId;
    private String action;
    private String description;
    private String ipAddress;
    private String userAgent;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant timestamp;
    
    // 默认构造函数
    public UserActivityDTO() {
    }
    
    // 构造函数
    public UserActivityDTO(String userId, String action) {
        this.userId = userId;
        this.action = action;
        this.timestamp = Instant.now();
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UserActivityDTO activityDTO;
        
        private Builder() {
            this.activityDTO = new UserActivityDTO();
        }
        
        public Builder id(String id) {
            activityDTO.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            activityDTO.userId = userId;
            return this;
        }
        
        public Builder action(String action) {
            activityDTO.action = action;
            return this;
        }
        
        public Builder description(String description) {
            activityDTO.description = description;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            activityDTO.ipAddress = ipAddress;
            return this;
        }
        
        public Builder userAgent(String userAgent) {
            activityDTO.userAgent = userAgent;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            activityDTO.timestamp = timestamp;
            return this;
        }
        
        public UserActivityDTO build() {
            return activityDTO;
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
        return timestamp != null && 
               timestamp.isAfter(Instant.now().minusSeconds(300)); // 5分钟内
    }
    
    public boolean isLoginAction() {
        return "LOGIN".equals(action);
    }
    
    public boolean isLogoutAction() {
        return "LOGOUT".equals(action);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserActivityDTO that = (UserActivityDTO) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "UserActivityDTO{" +
               "id='" + id + '\'' +
               ", userId='" + userId + '\'' +
               ", action='" + action + '\'' +
               ", description='" + description + '\'' +
               ", timestamp=" + timestamp +
               '}';
    }
}
