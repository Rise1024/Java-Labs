package com.javalaabs.webflux.domain.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * 用户实体类
 * 使用 R2DBC 进行响应式数据库访问
 */
@Table("users")
public class User {
    
    @Id
    private String id;
    
    @Column("name")
    private String name;
    
    @Column("email")
    private String email;
    
    @Column("avatar_url")
    private String avatarUrl;
    
    @Column("account_type")
    private String accountType;
    
    @Column("create_time")
    private Instant createTime;
    
    @Column("update_time")
    private Instant updateTime;
    
    @Column("is_active")
    private Boolean isActive;
    
    // 默认构造函数
    public User() {
        this.isActive = true;
        this.createTime = Instant.now();
        this.updateTime = Instant.now();
        this.accountType = "standard";
    }
    
    // 构造函数
    public User(String name, String email) {
        this();
        this.name = name;
        this.email = email;
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final User user;
        
        private Builder() {
            this.user = new User();
        }
        
        public Builder id(String id) {
            user.id = id;
            return this;
        }
        
        public Builder name(String name) {
            user.name = name;
            return this;
        }
        
        public Builder email(String email) {
            user.email = email;
            return this;
        }
        
        public Builder avatarUrl(String avatarUrl) {
            user.avatarUrl = avatarUrl;
            return this;
        }
        
        public Builder accountType(String accountType) {
            user.accountType = accountType;
            return this;
        }
        
        public Builder createTime(Instant createTime) {
            user.createTime = createTime;
            return this;
        }
        
        public Builder updateTime(Instant updateTime) {
            user.updateTime = updateTime;
            return this;
        }
        
        public Builder isActive(Boolean isActive) {
            user.isActive = isActive;
            return this;
        }
        
        public User build() {
            // 确保必要字段不为空
            Objects.requireNonNull(user.name, "用户名不能为空");
            Objects.requireNonNull(user.email, "邮箱不能为空");
            
            if (user.createTime == null) {
                user.createTime = Instant.now();
            }
            if (user.updateTime == null) {
                user.updateTime = Instant.now();
            }
            if (user.isActive == null) {
                user.isActive = true;
            }
            if (user.accountType == null) {
                user.accountType = "standard";
            }
            
            return user;
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
    public void updateLastModified() {
        this.updateTime = Instant.now();
    }
    
    public boolean isPremiumUser() {
        return "premium".equals(this.accountType);
    }
    
    public void deactivate() {
        this.isActive = false;
        updateLastModified();
    }
    
    public void activate() {
        this.isActive = true;
        updateLastModified();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
               Objects.equals(email, user.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
    
    @Override
    public String toString() {
        return "User{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", accountType='" + accountType + '\'' +
               ", isActive=" + isActive +
               ", createTime=" + createTime +
               '}';
    }
}
