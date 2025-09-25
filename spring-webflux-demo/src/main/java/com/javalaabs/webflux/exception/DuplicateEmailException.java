package com.javalaabs.webflux.exception;

/**
 * 邮箱重复异常
 */
public class DuplicateEmailException extends BusinessException {
    
    public DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "邮箱已存在: " + email);
    }
    
    public DuplicateEmailException(String message, Throwable cause) {
        super("DUPLICATE_EMAIL", message, cause);
    }
}
