package com.javalaabs.crypto.utils;

/**
 * 加密相关异常
 * 
 * @author JavaLabs
 */
public class CryptoException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public CryptoException(String message) {
        super(message);
    }
    
    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public CryptoException(Throwable cause) {
        super(cause);
    }
}
