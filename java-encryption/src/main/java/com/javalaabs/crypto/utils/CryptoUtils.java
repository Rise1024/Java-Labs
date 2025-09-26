package com.javalaabs.crypto.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 加密工具类 - 通用工具方法
 * 
 * @author JavaLabs
 * @since JDK17
 */
@Slf4j
public final class CryptoUtils {
    
    // JDK17新特性：HexFormat
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    
    // 私有构造器，防止实例化
    private CryptoUtils() {
        throw new AssertionError("工具类不能被实例化");
    }
    
    /**
     * 生成指定长度的安全随机字节数组
     * 
     * @param length 长度
     * @return 随机字节数组
     */
    public static byte[] generateSecureRandomBytes(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }
        
        try {
            byte[] bytes = new byte[length];
            SecureRandom.getInstanceStrong().nextBytes(bytes);
            log.debug("生成了{}字节的安全随机数", length);
            return bytes;
        } catch (Exception e) {
            log.error("生成安全随机数失败", e);
            throw new CryptoException("生成安全随机数失败", e);
        }
    }
    
    /**
     * 字节数组转十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return HEX_FORMAT.formatHex(bytes);
    }
    
    /**
     * 十六进制字符串转字节数组
     * 
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return new byte[0];
        }
        try {
            return HEX_FORMAT.parseHex(hex);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的十六进制字符串: " + hex, e);
        }
    }
    
    /**
     * 字节数组转Base64字符串
     * 
     * @param bytes 字节数组
     * @return Base64字符串
     */
    public static String bytesToBase64(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }
    
    /**
     * Base64字符串转字节数组
     * 
     * @param base64 Base64字符串
     * @return 字节数组
     */
    public static byte[] base64ToBytes(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(base64);
        } catch (Exception e) {
            throw new IllegalArgumentException("无效的Base64字符串: " + base64, e);
        }
    }
    
    /**
     * 字符串转字节数组（使用UTF-8编码）
     * 
     * @param text 字符串
     * @return 字节数组
     */
    public static byte[] stringToBytes(String text) {
        if (text == null) {
            return new byte[0];
        }
        return text.getBytes(StandardCharsets.UTF_8);
    }
    
    /**
     * 字节数组转字符串（使用UTF-8编码）
     * 
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String bytesToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }
    
    /**
     * 安全比较两个字节数组是否相等（防止时序攻击）
     * 
     * @param a 数组a
     * @param b 数组b
     * @return 是否相等
     */
    public static boolean secureEquals(byte[] a, byte[] b) {
        if (a == null || b == null) {
            return a == b;
        }
        
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * 清除字节数组中的敏感数据
     * 
     * @param bytes 字节数组
     */
    public static void clearSensitiveData(byte[] bytes) {
        if (bytes != null) {
            java.util.Arrays.fill(bytes, (byte) 0);
        }
    }
    
    /**
     * 清除字符数组中的敏感数据
     * 
     * @param chars 字符数组
     */
    public static void clearSensitiveData(char[] chars) {
        if (chars != null) {
            java.util.Arrays.fill(chars, '\0');
        }
    }
    
    /**
     * 验证密钥长度是否有效
     * 
     * @param keyLength 密钥长度（位）
     * @param validLengths 有效长度数组
     * @throws IllegalArgumentException 如果密钥长度无效
     */
    public static void validateKeyLength(int keyLength, int... validLengths) {
        for (int valid : validLengths) {
            if (keyLength == valid) {
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("无效的密钥长度: %d，有效长度: %s", keyLength, 
                         java.util.Arrays.toString(validLengths)));
    }
}
