package com.javalaabs.webflux.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 更新用户请求DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdateUserRequest {
    
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String name;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 500, message = "头像URL长度不能超过500个字符")
    private String avatarUrl;
    
    @Size(max = 20, message = "账户类型长度不能超过20个字符")
    private String accountType;
    
    private Boolean isActive;
    
    // 默认构造函数
    public UpdateUserRequest() {
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final UpdateUserRequest request;
        
        private Builder() {
            this.request = new UpdateUserRequest();
        }
        
        public Builder name(String name) {
            request.name = name;
            return this;
        }
        
        public Builder email(String email) {
            request.email = email;
            return this;
        }
        
        public Builder avatarUrl(String avatarUrl) {
            request.avatarUrl = avatarUrl;
            return this;
        }
        
        public Builder accountType(String accountType) {
            request.accountType = accountType;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            request.isActive = isActive;
            return this;
        }
        
        public UpdateUserRequest build() {
            return request;
        }
    }
    
    // Getter 和 Setter 方法
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // 业务方法
    public boolean hasName() {
        return name != null && !name.trim().isEmpty();
    }
    
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }
    
    public boolean hasAvatarUrl() {
        return avatarUrl != null && !avatarUrl.trim().isEmpty();
    }
    
    public boolean hasAccountType() {
        return accountType != null && !accountType.trim().isEmpty();
    }
    
    public boolean hasActiveStatus() {
        return isActive != null;
    }
    
    public void normalizeEmail() {
        if (this.email != null) {
            this.email = this.email.trim().toLowerCase();
        }
    }
    
    public void normalizeName() {
        if (this.name != null) {
            this.name = this.name.trim();
        }
    }
    
    public boolean isEmpty() {
        return !hasName() && !hasEmail() && !hasAvatarUrl() && 
               !hasAccountType() && !hasActiveStatus();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateUserRequest that = (UpdateUserRequest) o;
        return Objects.equals(name, that.name) &&
               Objects.equals(email, that.email) &&
               Objects.equals(avatarUrl, that.avatarUrl) &&
               Objects.equals(accountType, that.accountType) &&
               Objects.equals(isActive, that.isActive);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, email, avatarUrl, accountType, isActive);
    }
    
    @Override
    public String toString() {
        return "UpdateUserRequest{" +
               "name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", avatarUrl='" + avatarUrl + '\'' +
               ", accountType='" + accountType + '\'' +
               ", isActive=" + isActive +
               '}';
    }
}
