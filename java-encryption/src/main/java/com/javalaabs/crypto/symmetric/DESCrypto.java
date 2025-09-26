package com.javalaabs.crypto.symmetric;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * DES 和 3DES 对称加密算法实现
 * 
 * 注意：DES算法已被认为不安全，仅用于学习目的
 * 生产环境请使用AES算法
 * 
 * @author JavaLabs
 */
@Slf4j
public class DESCrypto {
    
    // DES算法常量
    private static final String DES_ALGORITHM = "DES";
    private static final String DES_CIPHER = "DES/CBC/PKCS5Padding";
    private static final int DES_KEY_LENGTH = 56; // 实际56位，存储为64位
    private static final int DES_IV_LENGTH = 8;
    
    // 3DES算法常量
    private static final String TRIPLE_DES_ALGORITHM = "DESede";
    private static final String TRIPLE_DES_CIPHER = "DESede/CBC/PKCS5Padding";
    private static final int TRIPLE_DES_IV_LENGTH = 8;
    
    /**
     * 生成DES密钥
     * 
     * @return Base64编码的密钥
     */
    public static String generateDESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(DES_ALGORITHM);
            SecretKey secretKey = keyGenerator.generateKey();
            
            String keyBase64 = CryptoUtils.bytesToBase64(secretKey.getEncoded());
            log.warn("生成DES密钥成功 - 注意：DES算法不安全，仅用于学习");
            return keyBase64;
            
        } catch (Exception e) {
            log.error("生成DES密钥失败", e);
            throw new CryptoException("生成DES密钥失败", e);
        }
    }
    
    /**
     * 生成3DES密钥
     * 
     * @param keySize 密钥长度（112或168位）
     * @return Base64编码的密钥
     */
    public static String generate3DESKey(int keySize) {
        if (keySize != 112 && keySize != 168) {
            throw new IllegalArgumentException("3DES密钥长度只能是112或168位");
        }
        
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(TRIPLE_DES_ALGORITHM);
            keyGenerator.init(keySize);
            SecretKey secretKey = keyGenerator.generateKey();
            
            String keyBase64 = CryptoUtils.bytesToBase64(secretKey.getEncoded());
            log.info("生成3DES-{}密钥成功", keySize);
            return keyBase64;
            
        } catch (Exception e) {
            log.error("生成3DES密钥失败", e);
            throw new CryptoException("生成3DES密钥失败", e);
        }
    }
    
    /**
     * DES加密
     * 
     * @param plainText 明文
     * @param keyBase64 Base64编码的密钥
     * @return 加密结果
     */
    public static DESResult encryptDES(String plainText, String keyBase64) {
        try {
            // 解析密钥
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, DES_ALGORITHM);
            
            // 生成随机IV
            byte[] iv = CryptoUtils.generateSecureRandomBytes(DES_IV_LENGTH);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(DES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // 执行加密
            byte[] cipherText = cipher.doFinal(CryptoUtils.stringToBytes(plainText));
            
            log.warn("DES加密完成 - 注意：DES算法不安全");
            
            return new DESResult(
                CryptoUtils.bytesToHex(iv),
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "DES"
            );
            
        } catch (Exception e) {
            log.error("DES加密失败", e);
            throw new CryptoException("DES加密失败", e);
        }
    }
    
    /**
     * DES解密
     * 
     * @param result 加密结果
     * @param keyBase64 Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decryptDES(DESResult result, String keyBase64) {
        try {
            // 解析密钥和数据
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, DES_ALGORITHM);
            
            byte[] iv = CryptoUtils.hexToBytes(result.ivHex());
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(DES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // 执行解密
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("DES解密完成");
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("DES解密失败", e);
            throw new CryptoException("DES解密失败", e);
        }
    }
    
    /**
     * 3DES加密
     * 
     * @param plainText 明文
     * @param keyBase64 Base64编码的密钥
     * @return 加密结果
     */
    public static DESResult encrypt3DES(String plainText, String keyBase64) {
        try {
            // 解析密钥
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, TRIPLE_DES_ALGORITHM);
            
            // 生成随机IV
            byte[] iv = CryptoUtils.generateSecureRandomBytes(TRIPLE_DES_IV_LENGTH);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(TRIPLE_DES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // 执行加密
            byte[] cipherText = cipher.doFinal(CryptoUtils.stringToBytes(plainText));
            
            log.info("3DES加密完成");
            
            return new DESResult(
                CryptoUtils.bytesToHex(iv),
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "3DES"
            );
            
        } catch (Exception e) {
            log.error("3DES加密失败", e);
            throw new CryptoException("3DES加密失败", e);
        }
    }
    
    /**
     * 3DES解密
     * 
     * @param result 加密结果
     * @param keyBase64 Base64编码的密钥
     * @return 解密后的明文
     */
    public static String decrypt3DES(DESResult result, String keyBase64) {
        try {
            // 解析密钥和数据
            byte[] keyBytes = CryptoUtils.base64ToBytes(keyBase64);
            SecretKey secretKey = new SecretKeySpec(keyBytes, TRIPLE_DES_ALGORITHM);
            
            byte[] iv = CryptoUtils.hexToBytes(result.ivHex());
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(TRIPLE_DES_CIPHER);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // 执行解密
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("3DES解密完成");
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("3DES解密失败", e);
            throw new CryptoException("3DES解密失败", e);
        }
    }
    
    /**
     * DES/3DES加密结果记录
     * 
     * @param ivHex 十六进制IV
     * @param cipherTextHex 十六进制密文
     * @param base64CipherText Base64密文
     * @param algorithm 算法类型（DES/3DES）
     */
    public record DESResult(String ivHex, String cipherTextHex, String base64CipherText, String algorithm) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public DESResult {
            if (ivHex == null || ivHex.isEmpty()) {
                throw new IllegalArgumentException("IV不能为空");
            }
            if (cipherTextHex == null || cipherTextHex.isEmpty()) {
                throw new IllegalArgumentException("密文不能为空");
            }
            if (base64CipherText == null || base64CipherText.isEmpty()) {
                throw new IllegalArgumentException("Base64密文不能为空");
            }
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("算法类型不能为空");
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
                    "iv": "%s",
                    "cipherText": "%s",
                    "base64": "%s"
                }
                """.formatted(algorithm, ivHex, cipherTextHex, base64CipherText);
        }
    }
}
