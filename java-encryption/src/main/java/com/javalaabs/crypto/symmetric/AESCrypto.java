package com.javalaabs.crypto.symmetric;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

/**
 * AES 对称加密算法实现
 * 支持 AES-CBC 和 AES-GCM 模式
 * 
 * @author JavaLabs
 */
@Slf4j
public class AESCrypto {
    
    // AES算法常量
    private static final String ALGORITHM = "AES";
    private static final String AES_CBC_CIPHER = "AES/CBC/PKCS5Padding";
    private static final String AES_GCM_CIPHER = "AES/GCM/NoPadding";
    
    // CBC模式IV长度
    private static final int CBC_IV_LENGTH = 16;
    // GCM模式IV长度
    private static final int GCM_IV_LENGTH = 12;
    // GCM模式标签长度
    private static final int GCM_TAG_LENGTH = 16;
    
    /**
     * 生成AES密钥
     * 
     * @param keySize 密钥长度（128, 192, 256）
     * @return Base64编码的密钥
     */
    public static String generateKey(int keySize) {
        CryptoUtils.validateKeyLength(keySize, 128, 192, 256);
        
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            
            String keyBase64 = CryptoUtils.bytesToBase64(secretKey.getEncoded());
            log.info("生成AES-{}密钥成功", keySize);
            return keyBase64;
            
        } catch (Exception e) {
            log.error("生成AES密钥失败", e);
            throw new CryptoException("生成AES密钥失败", e);
        }
    }
    
    /**
     * AES-CBC 加密
     * 
     * @param plainText 明文
     * @param keyBase64 Base64编码的密钥
     * @return 加密结果（包含IV和密文）
     */
    public static AESResult encryptCBC(String plainText, String keyBase64) {
        try {
            // 解析密钥
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // 生成随机IV
            byte[] iv = CryptoUtils.generateSecureRandomBytes(CBC_IV_LENGTH);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(AES_CBC_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // 执行加密
            byte[] cipherText = cipher.doFinal(CryptoUtils.stringToBytes(plainText));
            
            log.info("AES-CBC加密完成 - 明文长度: {}, 密文长度: {}", 
                    plainText.length(), cipherText.length);
            
            return new AESResult(
                CryptoUtils.bytesToHex(iv),
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "CBC"
            );
            
        } catch (Exception e) {
            log.error("AES-CBC加密失败", e);
            throw new CryptoException("AES-CBC加密失败", e);
        }
    }
    
    /**
     * AES-CBC 解密
     * 
     * @param result 加密结果
     * @param keyBase64 Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decryptCBC(AESResult result, String keyBase64) {
        try {
            // 解析密钥和数据
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            byte[] iv = CryptoUtils.hexToBytes(result.ivHex());
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(AES_CBC_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // 执行解密
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("AES-CBC解密完成 - 密文长度: {}, 明文长度: {}", 
                    cipherText.length, plainText.length);
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("AES-CBC解密失败", e);
            throw new CryptoException("AES-CBC解密失败", e);
        }
    }
    
    /**
     * AES-GCM 加密（认证加密）
     * 
     * @param plainText 明文
     * @param keyBase64 Base64编码的密钥
     * @return 加密结果（包含IV和密文+认证标签）
     */
    public static AESResult encryptGCM(String plainText, String keyBase64) {
        try {
            // 解析密钥
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            // 生成随机IV
            byte[] iv = CryptoUtils.generateSecureRandomBytes(GCM_IV_LENGTH);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(AES_GCM_CIPHER);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            
            // 执行加密
            byte[] cipherText = cipher.doFinal(CryptoUtils.stringToBytes(plainText));
            
            log.info("AES-GCM加密完成 - 明文长度: {}, 密文长度: {} (包含认证标签)", 
                    plainText.length(), cipherText.length);
            
            return new AESResult(
                CryptoUtils.bytesToHex(iv),
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "GCM"
            );
            
        } catch (Exception e) {
            log.error("AES-GCM加密失败", e);
            throw new CryptoException("AES-GCM加密失败", e);
        }
    }
    
    /**
     * AES-GCM 解密
     * 
     * @param result 加密结果
     * @param keyBase64 Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decryptGCM(AESResult result, String keyBase64) {
        try {
            // 解析密钥和数据
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            
            byte[] iv = CryptoUtils.hexToBytes(result.ivHex());
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(AES_GCM_CIPHER);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            
            // 执行解密（同时验证认证标签）
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("AES-GCM解密完成 - 密文长度: {}, 明文长度: {}", 
                    cipherText.length, plainText.length);
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("AES-GCM解密失败", e);
            throw new CryptoException("AES-GCM解密失败", e);
        }
    }
    
    /**
     * AES加密结果记录 - JDK17 Record特性
     * 
     * @param ivHex 十六进制IV
     * @param cipherTextHex 十六进制密文
     * @param base64CipherText Base64密文
     * @param mode 加密模式（CBC/GCM）
     */
    public record AESResult(String ivHex, String cipherTextHex, String base64CipherText, String mode) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public AESResult {
            if (ivHex == null || ivHex.isEmpty()) {
                throw new IllegalArgumentException("IV不能为空");
            }
            if (cipherTextHex == null || cipherTextHex.isEmpty()) {
                throw new IllegalArgumentException("密文不能为空");
            }
            if (base64CipherText == null || base64CipherText.isEmpty()) {
                throw new IllegalArgumentException("Base64密文不能为空");
            }
            if (mode == null || mode.isEmpty()) {
                throw new IllegalArgumentException("加密模式不能为空");
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
                    "mode": "%s",
                    "iv": "%s",
                    "cipherText": "%s",
                    "base64": "%s"
                }
                """.formatted(mode, ivHex, cipherTextHex, base64CipherText);
        }
    }
}
