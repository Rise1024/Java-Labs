package com.javalaabs.webflux.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户数据传输对象
 * 用于API响应的用户信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
    private String accountType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    private Instant updateTime;
    
    private Boolean isActive;
    
    // 默认构造函数
    public UserDTO() {
    }
    
    // 构造函数
    public UserDTO(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UserDTO userDTO;
        
        private Builder() {
            this.userDTO = new UserDTO();
        }
        
        public Builder id(String id) {
            userDTO.id = id;
            return this;
        }
        
        public Builder name(String name) {
            userDTO.name = name;
            return this;
        }
        
        public Builder email(String email) {
            userDTO.email = email;
            return this;
        }
        
        public Builder avatarUrl(String avatarUrl) {
            userDTO.avatarUrl = avatarUrl;
            return this;
        }
        
        public Builder accountType(String accountType) {
            userDTO.accountType = accountType;
            return this;
        }
        
        public Builder createTime(Instant createTime) {
            userDTO.createTime = createTime;
            return this;
        }
        
        public Builder updateTime(Instant updateTime) {
            userDTO.updateTime = updateTime;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            userDTO.isActive = isActive;
            return this;
        }
        
        public UserDTO build() {
            return userDTO;
        }
    }
    
    // Getter 和 Setter 方法
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    public Instant getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Instant createTime) {
        this.createTime = createTime;
    }
    
    public Instant getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // 业务方法
    public boolean isPremiumUser() {
        return "premium".equals(this.accountType);
    }
    
    public boolean isActiveUser() {
        return Boolean.TRUE.equals(this.isActive);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(id, userDTO.id) &&
               Objects.equals(email, userDTO.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "UserDTO{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", accountType='" + accountType + '\'' +
               ", isActive=" + isActive +
               '}';
    }
}
