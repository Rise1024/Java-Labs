package com.javalaabs.webflux.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Objects;

/**
 * 创建用户请求DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 50, message = "用户名长度必须在2-50个字符之间")
    private String name;
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 20, message = "账户类型长度不能超过20个字符")
    private String accountType = "standard";
    
    // 默认构造函数
    public CreateUserRequest() {
    }
    
    // 构造函数
    public CreateUserRequest(String name, String email) {
        this.name = name;
        this.email = email;
    }
    
    public CreateUserRequest(String name, String email, String accountType) {
        this.name = name;
        this.email = email;
        this.accountType = accountType;
    }
    
    // Builder 模式
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final CreateUserRequest request;
        
        private Builder() {
            this.request = new CreateUserRequest();
        }
        
        public Builder name(String name) {
            request.name = name;
            return this;
        }
        
        public Builder email(String email) {
            request.email = email;
            return this;
        }
        
        public Builder accountType(String accountType) {
            request.accountType = accountType;
            return this;
        }
        
        public CreateUserRequest build() {
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
    
    public String getAccountType() {
        return accountType;
    }
    
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    // 业务方法
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
    
    public boolean isPremiumAccount() {
        return "premium".equals(this.accountType);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateUserRequest that = (CreateUserRequest) o;
        return Objects.equals(email, that.email);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
    
    @Override
    public String toString() {
        return "CreateUserRequest{" +
               "name='" + name + '\'' +
               ", email='" + email + '\'' +
               ", accountType='" + accountType + '\'' +
               '}';
    }
}
