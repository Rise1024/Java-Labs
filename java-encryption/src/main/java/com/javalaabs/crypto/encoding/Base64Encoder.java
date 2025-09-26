package com.javalaabs.crypto.encoding;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Base64编码解码工具类
 * 支持标准Base64、URL安全Base64、MIME Base64等
 * 
 * @author JavaLabs
 */
@Slf4j
public final class Base64Encoder {
    
    // Base64字符集常量
    private static final String STANDARD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    private static final String URL_SAFE_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
    private static final char PADDING_CHAR = '=';
    
    // 私有构造器
    private Base64Encoder() {
        throw new AssertionError("工具类不能被实例化");
    }
    
    /**
     * 标准Base64编码
     * 
     * @param text 待编码文本
     * @return Base64编码字符串
     */
    public static String encodeStandard(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().encodeToString(bytes);
        
        log.debug("标准Base64编码: {} -> {}", text, encoded);
        return encoded;
    }
    
    /**
     * 标准Base64解码
     * 
     * @param encoded Base64编码字符串
     * @return 解码后的文本
     */
    public static String decodeStandard(String encoded) {
        if (encoded == null) {
            return null;
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            String text = new String(bytes, StandardCharsets.UTF_8);
            
            log.debug("标准Base64解码: {} -> {}", encoded, text);
            return text;
        } catch (IllegalArgumentException e) {
            log.error("Base64解码失败: {}", encoded, e);
            throw new CryptoException("无效的Base64字符串", e);
        }
    }
    
    /**
     * URL安全Base64编码（使用-和_替代+和/）
     * 
     * @param text 待编码文本
     * @return URL安全的Base64编码字符串
     */
    public static String encodeUrlSafe(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getUrlEncoder().encodeToString(bytes);
        
        log.debug("URL安全Base64编码: {} -> {}", text, encoded);
        return encoded;
    }
    
    /**
     * URL安全Base64解码
     * 
     * @param encoded URL安全的Base64编码字符串
     * @return 解码后的文本
     */
    public static String decodeUrlSafe(String encoded) {
        if (encoded == null) {
            return null;
        }
        
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(encoded);
            String text = new String(bytes, StandardCharsets.UTF_8);
            
            log.debug("URL安全Base64解码: {} -> {}", encoded, text);
            return text;
        } catch (IllegalArgumentException e) {
            log.error("URL安全Base64解码失败: {}", encoded, e);
            throw new CryptoException("无效的URL安全Base64字符串", e);
        }
    }
    
    /**
     * 无填充Base64编码（去除=号）
     * 
     * @param text 待编码文本
     * @return 无填充的Base64编码字符串
     */
    public static String encodeWithoutPadding(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getEncoder().withoutPadding().encodeToString(bytes);
        
        log.debug("无填充Base64编码: {} -> {}", text, encoded);
        return encoded;
    }
    
    /**
     * MIME Base64编码（每76个字符换行）
     * 
     * @param text 待编码文本
     * @return MIME格式的Base64编码字符串
     */
    public static String encodeMime(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String encoded = Base64.getMimeEncoder().encodeToString(bytes);
        
        log.debug("MIME Base64编码: {} -> {}", text, encoded);
        return encoded;
    }
    
    /**
     * MIME Base64解码
     * 
     * @param encoded MIME格式的Base64编码字符串
     * @return 解码后的文本
     */
    public static String decodeMime(String encoded) {
        if (encoded == null) {
            return null;
        }
        
        try {
            byte[] bytes = Base64.getMimeDecoder().decode(encoded);
            String text = new String(bytes, StandardCharsets.UTF_8);
            
            log.debug("MIME Base64解码: {} -> {}", encoded, text);
            return text;
        } catch (IllegalArgumentException e) {
            log.error("MIME Base64解码失败: {}", encoded, e);
            throw new CryptoException("无效的MIME Base64字符串", e);
        }
    }
    
    /**
     * 字节数组的Base64编码
     * 
     * @param bytes 字节数组
     * @return Base64编码字符串
     */
    public static String encodeBytes(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        
        String encoded = Base64.getEncoder().encodeToString(bytes);
        log.debug("字节数组Base64编码: {} 字节 -> {}", bytes.length, encoded);
        return encoded;
    }
    
    /**
     * Base64字符串解码为字节数组
     * 
     * @param encoded Base64编码字符串
     * @return 字节数组
     */
    public static byte[] decodeToBytes(String encoded) {
        if (encoded == null) {
            return null;
        }
        
        try {
            byte[] bytes = Base64.getDecoder().decode(encoded);
            log.debug("Base64解码为字节数组: {} -> {} 字节", encoded, bytes.length);
            return bytes;
        } catch (IllegalArgumentException e) {
            log.error("Base64解码为字节数组失败: {}", encoded, e);
            throw new CryptoException("无效的Base64字符串", e);
        }
    }
    
    /**
     * 检查字符串是否为有效的Base64格式
     * 
     * @param text 待检查的字符串
     * @return 是否为有效Base64
     */
    public static boolean isValidBase64(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            // 尝试解码
            Base64.getDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 检查字符串是否为URL安全的Base64格式
     * 
     * @param text 待检查的字符串
     * @return 是否为有效URL安全Base64
     */
    public static boolean isValidUrlSafeBase64(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        try {
            // 尝试URL安全解码
            Base64.getUrlDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * 手动Base64编码实现（教学用途）
     * 
     * @param input 输入字符串
     * @return Base64编码字符串
     */
    public static String manualEncode(String input) {
        if (input == null) {
            return null;
        }
        
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < bytes.length; i += 3) {
            // 处理3个字节为一组
            int group = 0;
            int groupSize = Math.min(3, bytes.length - i);
            
            for (int j = 0; j < groupSize; j++) {
                group = (group << 8) | (bytes[i + j] & 0xFF);
            }
            
            // 左移补齐到24位
            group <<= (3 - groupSize) * 8;
            
            // 分解为4个6位的值
            for (int j = 3; j >= 0; j--) {
                int index = (group >> (j * 6)) & 0x3F;
                if (j >= 4 - groupSize * 4 / 3) {
                    result.append(STANDARD_ALPHABET.charAt(index));
                } else {
                    result.append(PADDING_CHAR);
                }
            }
        }
        
        String encoded = result.toString();
        log.debug("手动Base64编码: {} -> {}", input, encoded);
        return encoded;
    }
    
    /**
     * 获取Base64编码的各种格式
     * 
     * @param text 输入文本
     * @return Base64编码结果
     */
    public static Base64EncodingResult getAllEncodings(String text) {
        if (text == null) {
            throw new IllegalArgumentException("输入文本不能为空");
        }
        
        return new Base64EncodingResult(
            text,
            encodeStandard(text),
            encodeUrlSafe(text),
            encodeWithoutPadding(text),
            encodeMime(text),
            manualEncode(text)
        );
    }
    
    /**
     * Base64编码分析
     * 
     * @param encoded Base64编码字符串
     * @return 分析结果
     */
    public static Base64AnalysisResult analyze(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("编码字符串不能为空");
        }
        
        boolean isStandard = isValidBase64(encoded);
        boolean isUrlSafe = isValidUrlSafeBase64(encoded);
        
        int length = encoded.length();
        int paddingCount = 0;
        
        // 计算填充字符数量
        for (int i = encoded.length() - 1; i >= 0 && encoded.charAt(i) == PADDING_CHAR; i--) {
            paddingCount++;
        }
        
        // 计算原始数据长度
        int originalLength = (length - paddingCount) * 3 / 4;
        
        return new Base64AnalysisResult(
            encoded,
            isStandard,
            isUrlSafe,
            length,
            paddingCount,
            originalLength
        );
    }
    
    /**
     * Base64编码结果记录
     * 
     * @param originalText 原始文本
     * @param standard 标准Base64
     * @param urlSafe URL安全Base64
     * @param withoutPadding 无填充Base64
     * @param mime MIME Base64
     * @param manual 手动编码Base64
     */
    public record Base64EncodingResult(
        String originalText,
        String standard,
        String urlSafe,
        String withoutPadding,
        String mime,
        String manual
    ) {
        
        /**
         * 转换为JSON格式
         * 
         * @return JSON字符串
         */
        public String toJson() {
            return """
                {
                    "originalText": "%s",
                    "standard": "%s",
                    "urlSafe": "%s",
                    "withoutPadding": "%s",
                    "mime": "%s",
                    "manual": "%s"
                }
                """.formatted(originalText, standard, urlSafe, withoutPadding, 
                              mime.replace("\r\n", "\\r\\n"), manual);
        }
        
        /**
         * 转换为表格格式
         * 
         * @return 表格字符串
         */
        public String toTable() {
            return """
                ┌─────────────────┬─────────────────────────────────────┐
                │   编码类型      │                 值                  │
                ├─────────────────┼─────────────────────────────────────┤
                │ 原始文本        │ %-35s │
                │ 标准Base64      │ %-35s │
                │ URL安全Base64   │ %-35s │
                │ 无填充Base64    │ %-35s │
                │ MIME Base64     │ %-35s │
                │ 手动编码Base64  │ %-35s │
                └─────────────────┴─────────────────────────────────────┘
                """.formatted(originalText, standard, urlSafe, withoutPadding, 
                              mime.replace("\r\n", " "), manual);
        }
    }
    
    /**
     * Base64分析结果记录
     * 
     * @param encoded 编码字符串
     * @param isStandardValid 是否为有效标准Base64
     * @param isUrlSafeValid 是否为有效URL安全Base64
     * @param encodedLength 编码后长度
     * @param paddingCount 填充字符数量
     * @param originalLength 原始数据长度
     */
    public record Base64AnalysisResult(
        String encoded,
        boolean isStandardValid,
        boolean isUrlSafeValid,
        int encodedLength,
        int paddingCount,
        int originalLength
    ) {
        
        /**
         * 转换为JSON格式
         * 
         * @return JSON字符串
         */
        public String toJson() {
            return """
                {
                    "encoded": "%s",
                    "isStandardValid": %b,
                    "isUrlSafeValid": %b,
                    "encodedLength": %d,
                    "paddingCount": %d,
                    "originalLength": %d
                }
                """.formatted(encoded, isStandardValid, isUrlSafeValid, 
                              encodedLength, paddingCount, originalLength);
        }
        
        /**
         * 转换为详细分析报告
         * 
         * @return 分析报告字符串
         */
        public String toAnalysisReport() {
            return """
                Base64编码分析报告:
                ├─ 编码字符串: %s
                ├─ 标准Base64: %s
                ├─ URL安全Base64: %s
                ├─ 编码长度: %d 字符
                ├─ 填充字符: %d 个
                ├─ 原始数据长度: %d 字节
                └─ 编码效率: %.2f%%
                """.formatted(encoded, 
                             isStandardValid ? "✓ 有效" : "✗ 无效",
                             isUrlSafeValid ? "✓ 有效" : "✗ 无效",
                             encodedLength, paddingCount, originalLength,
                             (double) originalLength / encodedLength * 100);
        }
    }
}
