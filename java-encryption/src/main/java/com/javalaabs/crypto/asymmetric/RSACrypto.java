package com.javalaabs.crypto.asymmetric;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSA 非对称加密算法实现
 * 
 * @author JavaLabs
 */
@Slf4j
public class RSACrypto {
    
    private static final String ALGORITHM = "RSA";
    private static final String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    // RSA/ECB/OAEPWithSHA-256AndMGF1Padding 更安全但兼容性较差
    
    /**
     * 生成RSA密钥对
     * 
     * @param keySize 密钥长度（1024, 2048, 3072, 4096）
     * @return RSA密钥对
     */
    public static RSAKeyPair generateKeyPair(int keySize) {
        CryptoUtils.validateKeyLength(keySize, 1024, 2048, 3072, 4096);
        
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(keySize);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            
            String publicKeyBase64 = CryptoUtils.bytesToBase64(publicKey.getEncoded());
            String privateKeyBase64 = CryptoUtils.bytesToBase64(privateKey.getEncoded());
            
            log.info("生成RSA-{}密钥对成功", keySize);
            
            return new RSAKeyPair(publicKeyBase64, privateKeyBase64, keySize);
            
        } catch (Exception e) {
            log.error("生成RSA密钥对失败", e);
            throw new CryptoException("生成RSA密钥对失败", e);
        }
    }
    
    /**
     * RSA公钥加密
     * 
     * @param plainText 明文
     * @param publicKeyBase64 Base64编码的公钥
     * @return 加密结果
     */
    public static RSAResult encryptWithPublicKey(String plainText, String publicKeyBase64) {
        try {
            // 解析公钥
            byte[] publicKeyBytes = CryptoUtils.base64ToBytes(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            
            // 执行加密
            byte[] plainTextBytes = CryptoUtils.stringToBytes(plainText);
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            
            log.info("RSA公钥加密完成 - 明文长度: {}, 密文长度: {}", 
                    plainText.length(), cipherText.length);
            
            return new RSAResult(
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "PUBLIC_KEY_ENCRYPT"
            );
            
        } catch (Exception e) {
            log.error("RSA公钥加密失败", e);
            throw new CryptoException("RSA公钥加密失败", e);
        }
    }
    
    /**
     * RSA私钥解密
     * 
     * @param result 加密结果
     * @param privateKeyBase64 Base64编码的私钥
     * @return 解密后的明文
     */
    public static String decryptWithPrivateKey(RSAResult result, String privateKeyBase64) {
        try {
            // 解析私钥
            byte[] privateKeyBytes = CryptoUtils.base64ToBytes(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            
            // 执行解密
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("RSA私钥解密完成");
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("RSA私钥解密失败", e);
            throw new CryptoException("RSA私钥解密失败", e);
        }
    }
    
    /**
     * RSA私钥加密（用于数字签名）
     * 
     * @param plainText 明文
     * @param privateKeyBase64 Base64编码的私钥
     * @return 加密结果
     */
    public static RSAResult encryptWithPrivateKey(String plainText, String privateKeyBase64) {
        try {
            // 解析私钥
            byte[] privateKeyBytes = CryptoUtils.base64ToBytes(privateKeyBase64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // 初始化加密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            
            // 执行加密
            byte[] plainTextBytes = CryptoUtils.stringToBytes(plainText);
            byte[] cipherText = cipher.doFinal(plainTextBytes);
            
            log.info("RSA私钥加密完成");
            
            return new RSAResult(
                CryptoUtils.bytesToHex(cipherText),
                CryptoUtils.bytesToBase64(cipherText),
                "PRIVATE_KEY_ENCRYPT"
            );
            
        } catch (Exception e) {
            log.error("RSA私钥加密失败", e);
            throw new CryptoException("RSA私钥加密失败", e);
        }
    }
    
    /**
     * RSA公钥解密（用于验证数字签名）
     * 
     * @param result 加密结果
     * @param publicKeyBase64 Base64编码的公钥
     * @return 解密后的明文
     */
    public static String decryptWithPublicKey(RSAResult result, String publicKeyBase64) {
        try {
            // 解析公钥
            byte[] publicKeyBytes = CryptoUtils.base64ToBytes(publicKeyBase64);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            
            // 初始化解密器
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            
            // 执行解密
            byte[] cipherText = CryptoUtils.hexToBytes(result.cipherTextHex());
            byte[] plainText = cipher.doFinal(cipherText);
            
            log.info("RSA公钥解密完成");
            
            return CryptoUtils.bytesToString(plainText);
            
        } catch (Exception e) {
            log.error("RSA公钥解密失败", e);
            throw new CryptoException("RSA公钥解密失败", e);
        }
    }
    
    /**
     * RSA密钥对记录
     * 
     * @param publicKey Base64编码的公钥
     * @param privateKey Base64编码的私钥
     * @param keySize 密钥长度
     */
    public record RSAKeyPair(String publicKey, String privateKey, int keySize) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public RSAKeyPair {
            if (publicKey == null || publicKey.isEmpty()) {
                throw new IllegalArgumentException("公钥不能为空");
            }
            if (privateKey == null || privateKey.isEmpty()) {
                throw new IllegalArgumentException("私钥不能为空");
            }
            if (keySize <= 0) {
                throw new IllegalArgumentException("密钥长度必须大于0");
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
                    "keySize": %d,
                    "publicKey": "%s",
                    "privateKey": "%s"
                }
                """.formatted(keySize, publicKey, privateKey);
        }
        
        /**
         * 只返回公钥信息（安全考虑）
         * 
         * @return 公钥JSON
         */
        public String getPublicKeyJson() {
            return """
                {
                    "keySize": %d,
                    "publicKey": "%s"
                }
                """.formatted(keySize, publicKey);
        }
    }
    
    /**
     * RSA加密结果记录
     * 
     * @param cipherTextHex 十六进制密文
     * @param base64CipherText Base64密文
     * @param operation 操作类型
     */
    public record RSAResult(String cipherTextHex, String base64CipherText, String operation) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public RSAResult {
            if (cipherTextHex == null || cipherTextHex.isEmpty()) {
                throw new IllegalArgumentException("密文不能为空");
            }
            if (base64CipherText == null || base64CipherText.isEmpty()) {
                throw new IllegalArgumentException("Base64密文不能为空");
            }
            if (operation == null || operation.isEmpty()) {
                throw new IllegalArgumentException("操作类型不能为空");
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
                    "operation": "%s",
                    "cipherText": "%s",
                    "base64": "%s"
                }
                """.formatted(operation, cipherTextHex, base64CipherText);
        }
    }
}
