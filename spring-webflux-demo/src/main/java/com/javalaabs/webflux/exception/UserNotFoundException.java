package com.javalaabs.webflux.exception;

/**
 * 用户不存在异常
 */
public class UserNotFoundException extends BusinessException {
    
    public UserNotFoundException(String userId) {
        super("USER_NOT_FOUND", "用户不存在: " + userId);
    }
    
    public UserNotFoundException(String message, Throwable cause) {
        super("USER_NOT_FOUND", message, cause);
    }
}
