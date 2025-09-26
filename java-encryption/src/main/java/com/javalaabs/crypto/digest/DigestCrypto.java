package com.javalaabs.crypto.digest;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * 消息摘要算法实现
 * 包括 MD5、SHA 系列、HMAC 等
 * 
 * @author JavaLabs
 */
@Slf4j
public class DigestCrypto {
    
    // 摘要算法常量
    private static final String MD5 = "MD5";
    private static final String SHA1 = "SHA-1";
    private static final String SHA256 = "SHA-256";
    private static final String SHA384 = "SHA-384";
    private static final String SHA512 = "SHA-512";
    private static final String SHA3_256 = "SHA3-256";
    private static final String SHA3_512 = "SHA3-512";
    
    // HMAC算法常量
    private static final String HMAC_MD5 = "HmacMD5";
    private static final String HMAC_SHA1 = "HmacSHA1";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String HMAC_SHA512 = "HmacSHA512";
    
    /**
     * MD5摘要（不推荐用于安全场景）
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult md5(String input) {
        log.warn("MD5算法存在安全风险，不推荐用于安全场景");
        return digest(input, MD5);
    }
    
    /**
     * SHA-1摘要（不推荐用于安全场景）
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha1(String input) {
        log.warn("SHA-1算法存在安全风险，不推荐用于安全场景");
        return digest(input, SHA1);
    }
    
    /**
     * SHA-256摘要（推荐）
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha256(String input) {
        return digest(input, SHA256);
    }
    
    /**
     * SHA-384摘要
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha384(String input) {
        return digest(input, SHA384);
    }
    
    /**
     * SHA-512摘要
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha512(String input) {
        return digest(input, SHA512);
    }
    
    /**
     * SHA3-256摘要（最新标准）
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha3_256(String input) {
        return digest(input, SHA3_256);
    }
    
    /**
     * SHA3-512摘要（最新标准）
     * 
     * @param input 输入文本
     * @return 摘要结果
     */
    public static DigestResult sha3_512(String input) {
        return digest(input, SHA3_512);
    }
    
    /**
     * 通用摘要方法
     * 
     * @param input 输入文本
     * @param algorithm 算法名称
     * @return 摘要结果
     */
    private static DigestResult digest(String input, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] inputBytes = CryptoUtils.stringToBytes(input);
            byte[] hashBytes = digest.digest(inputBytes);
            
            log.info("{}摘要完成 - 输入长度: {}, 摘要长度: {} bytes", 
                    algorithm, input.length(), hashBytes.length);
            
            return new DigestResult(
                CryptoUtils.bytesToHex(hashBytes),
                CryptoUtils.bytesToBase64(hashBytes),
                algorithm,
                hashBytes.length * 8 // 位长度
            );
            
        } catch (Exception e) {
            log.error("{}摘要计算失败", algorithm, e);
            throw new CryptoException(algorithm + "摘要计算失败", e);
        }
    }
    
    /**
     * 生成HMAC密钥
     * 
     * @param keyLength 密钥长度（字节）
     * @return Base64编码的密钥
     */
    public static String generateHMACKey(int keyLength) {
        if (keyLength <= 0) {
            throw new IllegalArgumentException("密钥长度必须大于0");
        }
        
        byte[] keyBytes = CryptoUtils.generateSecureRandomBytes(keyLength);
        String keyBase64 = CryptoUtils.bytesToBase64(keyBytes);
        
        log.info("生成HMAC密钥成功 - 长度: {} bytes", keyLength);
        return keyBase64;
    }
    
    /**
     * HMAC-MD5（不推荐用于安全场景）
     * 
     * @param input 输入文本
     * @param keyBase64 Base64编码的密钥
     * @return HMAC结果
     */
    public static DigestResult hmacMD5(String input, String keyBase64) {
        log.warn("HMAC-MD5存在安全风险，不推荐用于安全场景");
        return hmac(input, keyBase64, HMAC_MD5);
    }
    
    /**
     * HMAC-SHA1（不推荐用于安全场景）
     * 
     * @param input 输入文本
     * @param keyBase64 Base64编码的密钥
     * @return HMAC结果
     */
    public static DigestResult hmacSHA1(String input, String keyBase64) {
        log.warn("HMAC-SHA1存在安全风险，不推荐用于安全场景");
        return hmac(input, keyBase64, HMAC_SHA1);
    }
    
    /**
     * HMAC-SHA256（推荐）
     * 
     * @param input 输入文本
     * @param keyBase64 Base64编码的密钥
     * @return HMAC结果
     */
    public static DigestResult hmacSHA256(String input, String keyBase64) {
        return hmac(input, keyBase64, HMAC_SHA256);
    }
    
    /**
     * HMAC-SHA512
     * 
     * @param input 输入文本
     * @param keyBase64 Base64编码的密钥
     * @return HMAC结果
     */
    public static DigestResult hmacSHA512(String input, String keyBase64) {
        return hmac(input, keyBase64, HMAC_SHA512);
    }
    
    /**
     * 通用HMAC方法
     * 
     * @param input 输入文本
     * @param keyBase64 Base64编码的密钥
     * @param algorithm HMAC算法名称
     * @return HMAC结果
     */
    private static DigestResult hmac(String input, String keyBase64, String algorithm) {
        try {
            // 解析密钥
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, algorithm);
            
            // 初始化MAC
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKey);
            
            // 计算HMAC
            byte[] inputBytes = CryptoUtils.stringToBytes(input);
            byte[] hmacBytes = mac.doFinal(inputBytes);
            
            log.info("{}计算完成 - 输入长度: {}, HMAC长度: {} bytes", 
                    algorithm, input.length(), hmacBytes.length);
            
            return new DigestResult(
                CryptoUtils.bytesToHex(hmacBytes),
                CryptoUtils.bytesToBase64(hmacBytes),
                algorithm,
                hmacBytes.length * 8
            );
            
        } catch (Exception e) {
            log.error("{}计算失败", algorithm, e);
            throw new CryptoException(algorithm + "计算失败", e);
        }
    }
    
    /**
     * 验证摘要是否匹配
     * 
     * @param input 原始输入
     * @param expectedDigest 期望的摘要结果
     * @param algorithm 算法名称
     * @return 是否匹配
     */
    public static boolean verifyDigest(String input, DigestResult expectedDigest, String algorithm) {
        try {
            DigestResult actualDigest = digest(input, algorithm);
            boolean matches = actualDigest.hexHash().equalsIgnoreCase(expectedDigest.hexHash());
            
            log.info("摘要验证结果: {}", matches ? "匹配" : "不匹配");
            return matches;
            
        } catch (Exception e) {
            log.error("摘要验证失败", e);
            return false;
        }
    }
    
    /**
     * 验证HMAC是否匹配
     * 
     * @param input 原始输入
     * @param keyBase64 Base64编码的密钥
     * @param expectedHmac 期望的HMAC结果
     * @param algorithm HMAC算法名称
     * @return 是否匹配
     */
    public static boolean verifyHMAC(String input, String keyBase64, DigestResult expectedHmac, String algorithm) {
        try {
            DigestResult actualHmac = hmac(input, keyBase64, algorithm);
            boolean matches = CryptoUtils.secureEquals(
                CryptoUtils.hexToBytes(actualHmac.hexHash()),
                CryptoUtils.hexToBytes(expectedHmac.hexHash())
            );
            
            log.info("HMAC验证结果: {}", matches ? "匹配" : "不匹配");
            return matches;
            
        } catch (Exception e) {
            log.error("HMAC验证失败", e);
            return false;
        }
    }
    
    /**
     * 摘要结果记录
     * 
     * @param hexHash 十六进制摘要
     * @param base64Hash Base64摘要
     * @param algorithm 算法名称
     * @param bitLength 摘要位长度
     */
    public record DigestResult(String hexHash, String base64Hash, String algorithm, int bitLength) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public DigestResult {
            if (hexHash == null || hexHash.isEmpty()) {
                throw new IllegalArgumentException("十六进制摘要不能为空");
            }
            if (base64Hash == null || base64Hash.isEmpty()) {
                throw new IllegalArgumentException("Base64摘要不能为空");
            }
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("算法名称不能为空");
            }
            if (bitLength <= 0) {
                throw new IllegalArgumentException("摘要位长度必须大于0");
            }
        }
        
        /**
         * 转换为JSON格式
         * 
         * @return JSON字符串
         */
        public String toJson() {
            return """
                {
                    "algorithm": "%s",
                    "bitLength": %d,
                    "hexHash": "%s",
                    "base64Hash": "%s"
                }
                """.formatted(algorithm, bitLength, hexHash, base64Hash);
        }
        
        /**
         * 获取摘要字节数组
         * 
         * @return 摘要字节数组
         */
        public byte[] getHashBytes() {
            return CryptoUtils.hexToBytes(hexHash);
        }
    }
}
