package com.javalaabs.crypto;

import com.javalaabs.crypto.asymmetric.RSACrypto;
import com.javalaabs.crypto.digest.DigestCrypto;
import com.javalaabs.crypto.encoding.BaseConverter;
import com.javalaabs.crypto.encoding.Base64Encoder;
import com.javalaabs.crypto.encoding.TextEncoder;
import com.javalaabs.crypto.encoding.UrlEncoder;
import com.javalaabs.crypto.password.PasswordHashCrypto;
import com.javalaabs.crypto.symmetric.AESCrypto;
import com.javalaabs.crypto.symmetric.DESCrypto;
import lombok.extern.slf4j.Slf4j;

/**
 * 加密算法综合演示
 * 
 * @author JavaLabs
 */
@Slf4j
public class CryptoDemo {
    
    public static void main(String[] args) {
        log.info("=== 开始加密算法演示 ===");
        
        // 演示对称加密
        demonstrateSymmetricCrypto();
        
        // 演示非对称加密
        demonstrateAsymmetricCrypto();
        
        // 演示消息摘要
        demonstrateDigestCrypto();
        
        // 演示密码哈希
        demonstratePasswordHashing();
        
        // 演示编码功能
        demonstrateEncodingFunctions();
        
        log.info("=== 加密算法演示完成 ===");
    }
    
    /**
     * 演示对称加密算法
     */
    private static void demonstrateSymmetricCrypto() {
        log.info("\n=== 对称加密算法演示 ===");
        
        String plainText = "这是一个测试消息 - Hello Crypto World! 🔐";
        
        try {
            // AES-256-GCM 演示
            log.info("1. AES-256-GCM 演示:");
            String aesKey = AESCrypto.generateKey(256);
            log.info("生成的AES密钥: {}", aesKey.substring(0, 20) + "...");
            
            var aesGcmResult = AESCrypto.encryptGCM(plainText, aesKey);
            log.info("GCM加密结果: {}", aesGcmResult.toJson());
            
            String aesDecrypted = AESCrypto.decryptGCM(aesGcmResult, aesKey);
            log.info("GCM解密结果: {}", aesDecrypted);
            log.info("AES-GCM 验证: {}", plainText.equals(aesDecrypted) ? "通过" : "失败");
            
            // AES-256-CBC 演示
            log.info("\n2. AES-256-CBC 演示:");
            var aesCbcResult = AESCrypto.encryptCBC(plainText, aesKey);
            log.info("CBC加密结果: {}", aesCbcResult.toJson());
            
            String aesCbcDecrypted = AESCrypto.decryptCBC(aesCbcResult, aesKey);
            log.info("CBC解密结果: {}", aesCbcDecrypted);
            log.info("AES-CBC 验证: {}", plainText.equals(aesCbcDecrypted) ? "通过" : "失败");
            
            // 3DES 演示
            log.info("\n3. 3DES 演示:");
            String tripleDesKey = DESCrypto.generate3DESKey(168);
            log.info("生成的3DES密钥: {}", tripleDesKey.substring(0, 20) + "...");
            
            var tripleDesResult = DESCrypto.encrypt3DES(plainText, tripleDesKey);
            log.info("3DES加密结果: {}", tripleDesResult.toJson());
            
            String tripleDesDecrypted = DESCrypto.decrypt3DES(tripleDesResult, tripleDesKey);
            log.info("3DES解密结果: {}", tripleDesDecrypted);
            log.info("3DES 验证: {}", plainText.equals(tripleDesDecrypted) ? "通过" : "失败");
            
        } catch (Exception e) {
            log.error("对称加密演示失败", e);
        }
    }
    
    /**
     * 演示非对称加密算法
     */
    private static void demonstrateAsymmetricCrypto() {
        log.info("\n=== 非对称加密算法演示 ===");
        
        String plainText = "RSA加密测试消息";
        
        try {
            // RSA 演示
            log.info("1. RSA-2048 演示:");
            var rsaKeyPair = RSACrypto.generateKeyPair(2048);
            log.info("RSA密钥对生成完成");
            log.info("公钥: {}", rsaKeyPair.getPublicKeyJson());
            
            // 公钥加密，私钥解密
            var rsaResult = RSACrypto.encryptWithPublicKey(plainText, rsaKeyPair.publicKey());
            log.info("RSA公钥加密结果: {}", rsaResult.toJson());
            
            String rsaDecrypted = RSACrypto.decryptWithPrivateKey(rsaResult, rsaKeyPair.privateKey());
            log.info("RSA私钥解密结果: {}", rsaDecrypted);
            log.info("RSA 验证: {}", plainText.equals(rsaDecrypted) ? "通过" : "失败");
            
            // 私钥加密，公钥解密（数字签名原理）
            log.info("\n2. RSA数字签名原理演示:");
            var signResult = RSACrypto.encryptWithPrivateKey(plainText, rsaKeyPair.privateKey());
            log.info("RSA私钥加密结果: {}", signResult.toJson());
            
            String signDecrypted = RSACrypto.decryptWithPublicKey(signResult, rsaKeyPair.publicKey());
            log.info("RSA公钥解密结果: {}", signDecrypted);
            log.info("RSA签名验证: {}", plainText.equals(signDecrypted) ? "通过" : "失败");
            
        } catch (Exception e) {
            log.error("非对称加密演示失败", e);
        }
    }
    
    /**
     * 演示消息摘要算法
     */
    private static void demonstrateDigestCrypto() {
        log.info("\n=== 消息摘要算法演示 ===");
        
        String message = "Hello Digest World! 这是一个测试消息。";
        
        try {
            // SHA-256 演示
            log.info("1. SHA-256 演示:");
            var sha256Result = DigestCrypto.sha256(message);
            log.info("SHA-256结果: {}", sha256Result.toJson());
            
            // SHA-512 演示
            log.info("\n2. SHA-512 演示:");
            var sha512Result = DigestCrypto.sha512(message);
            log.info("SHA-512结果: {}", sha512Result.toJson());
            
            // SHA3-256 演示
            log.info("\n3. SHA3-256 演示:");
            var sha3Result = DigestCrypto.sha3_256(message);
            log.info("SHA3-256结果: {}", sha3Result.toJson());
            
            // HMAC-SHA256 演示
            log.info("\n4. HMAC-SHA256 演示:");
            String hmacKey = DigestCrypto.generateHMACKey(32);
            log.info("HMAC密钥: {}", hmacKey.substring(0, 20) + "...");
            
            var hmacResult = DigestCrypto.hmacSHA256(message, hmacKey);
            log.info("HMAC-SHA256结果: {}", hmacResult.toJson());
            
            // 验证HMAC
            boolean hmacValid = DigestCrypto.verifyHMAC(message, hmacKey, hmacResult, "HmacSHA256");
            log.info("HMAC验证: {}", hmacValid ? "通过" : "失败");
            
        } catch (Exception e) {
            log.error("消息摘要演示失败", e);
        }
    }
    
    /**
     * 演示密码哈希算法
     */
    private static void demonstratePasswordHashing() {
        log.info("\n=== 密码哈希算法演示 ===");
        
        String password = "MySecurePassword123!@#";
        
        try {
            // PBKDF2 演示
            log.info("1. PBKDF2 演示:");
            var pbkdf2Result = PasswordHashCrypto.pbkdf2Hash(password);
            log.info("PBKDF2结果: {}", pbkdf2Result.toJson());
            
            // 验证PBKDF2密码
            boolean pbkdf2Valid = PasswordHashCrypto.verifyPBKDF2Password(password, pbkdf2Result);
            log.info("PBKDF2验证: {}", pbkdf2Valid ? "通过" : "失败");
            
            // 错误密码验证
            boolean pbkdf2Invalid = PasswordHashCrypto.verifyPBKDF2Password("WrongPassword", pbkdf2Result);
            log.info("PBKDF2错误密码验证: {}", pbkdf2Invalid ? "失败(应该失败)" : "通过");
            
            // BCrypt 演示
            log.info("\n2. BCrypt 演示:");
            var bcryptResult = PasswordHashCrypto.bcryptHash(password, 12);
            log.info("BCrypt结果: {}", bcryptResult.toJson());
            
            // 验证BCrypt密码
            boolean bcryptValid = PasswordHashCrypto.verifyBCryptPassword(password, bcryptResult.hash());
            log.info("BCrypt验证: {}", bcryptValid ? "通过" : "失败");
            
            // 错误密码验证
            boolean bcryptInvalid = PasswordHashCrypto.verifyBCryptPassword("WrongPassword", bcryptResult.hash());
            log.info("BCrypt错误密码验证: {}", bcryptInvalid ? "失败(应该失败)" : "通过");
            
            // 随机密码生成
            log.info("\n3. 安全密码生成演示:");
            String randomPassword = PasswordHashCrypto.generateSecurePassword(16, true, true, true, true);
            log.info("生成的安全密码: {}", randomPassword);
            
        } catch (Exception e) {
            log.error("密码哈希演示失败", e);
        }
    }
    
    /**
     * 演示编码功能
     */
    private static void demonstrateEncodingFunctions() {
        log.info("\n=== 编码功能演示 ===");
        
        String testText = "Hello 编码世界! 🌍";
        long testNumber = 123456789L;
        String testUrl = "https://example.com/search?q=java编程&type=tutorial";
        
        try {
            // 进制转换演示
            log.info("1. 进制转换演示:");
            log.info("测试数字: {}", testNumber);
            
            var baseResult = BaseConverter.getAllBaseConversions(testNumber);
            log.info("进制转换结果: {}", baseResult.toJson());
            
            // Base64编码演示
            log.info("\n2. Base64编码演示:");
            log.info("测试文本: {}", testText);
            
            var base64Result = Base64Encoder.getAllEncodings(testText);
            log.info("Base64编码结果: {}", base64Result.toJson());
            
            // 验证Base64解码
            String decoded = Base64Encoder.decodeStandard(base64Result.standard());
            log.info("Base64解码验证: {}", testText.equals(decoded) ? "通过" : "失败");
            
            // URL编码演示
            log.info("\n3. URL编码演示:");
            log.info("测试URL: {}", testUrl);
            
            var urlResult = UrlEncoder.encodeFullUrl(testUrl);
            log.info("URL编码结果: {}", urlResult.toJson());
            
            // 文本编码演示
            log.info("\n4. 文本编码演示:");
            log.info("测试文本: {}", testText);
            
            var textResult = TextEncoder.getAllEncodings(testText);
            log.info("文本编码结果: {}", textResult.toJson());
            
            // 编码检测演示
            log.info("\n5. 编码检测演示:");
            String hexText = TextEncoder.toHexUpper("Hello");
            var detectionResult = TextEncoder.detectEncoding(hexText);
            log.info("检测结果: {}", detectionResult.toDetectionReport());
            
            // Base62短链接演示
            log.info("\n6. Base62短链接演示:");
            String base62 = BaseConverter.encodeBase62(testNumber);
            long decodedNumber = BaseConverter.decodeBase62(base62);
            log.info("原始数字: {} -> Base62: {} -> 解码: {}", testNumber, base62, decodedNumber);
            log.info("Base62验证: {}", testNumber == decodedNumber ? "通过" : "失败");
            
        } catch (Exception e) {
            log.error("编码功能演示失败", e);
        }
    }
}
