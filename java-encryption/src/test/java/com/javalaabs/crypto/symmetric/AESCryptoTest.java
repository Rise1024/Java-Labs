package com.javalaabs.crypto.symmetric;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AES加密算法测试
 * 
 * @author JavaLabs
 */
class AESCryptoTest {
    
    private String testPlainText;
    private String aes256Key;
    
    @BeforeEach
    void setUp() {
        testPlainText = "这是一个测试加密的中文字符串 - Hello AES! 🔐";
        aes256Key = AESCrypto.generateKey(256);
    }
    
    @Test
    @DisplayName("测试AES密钥生成")
    void testGenerateKey() {
        // 测试不同密钥长度
        String key128 = AESCrypto.generateKey(128);
        String key192 = AESCrypto.generateKey(192);
        String key256 = AESCrypto.generateKey(256);
        
        assertNotNull(key128);
        assertNotNull(key192);
        assertNotNull(key256);
        
        // 验证密钥唯一性
        assertNotEquals(key128, key192);
        assertNotEquals(key192, key256);
        
        // 测试无效密钥长度
        assertThrows(IllegalArgumentException.class, () -> AESCrypto.generateKey(64));
    }
    
    @Test
    @DisplayName("测试AES-GCM加密解密")
    void testAESGCMEncryptDecrypt() {
        // 加密
        var encryptResult = AESCrypto.encryptGCM(testPlainText, aes256Key);
        
        assertNotNull(encryptResult);
        assertNotNull(encryptResult.ivHex());
        assertNotNull(encryptResult.cipherTextHex());
        assertEquals("GCM", encryptResult.mode());
        
        // 解密
        String decryptedText = AESCrypto.decryptGCM(encryptResult, aes256Key);
        assertEquals(testPlainText, decryptedText);
    }
    
    @Test
    @DisplayName("测试AES-CBC加密解密")
    void testAESCBCEncryptDecrypt() {
        // 加密
        var encryptResult = AESCrypto.encryptCBC(testPlainText, aes256Key);
        
        assertNotNull(encryptResult);
        assertNotNull(encryptResult.ivHex());
        assertNotNull(encryptResult.cipherTextHex());
        assertEquals("CBC", encryptResult.mode());
        
        // 解密
        String decryptedText = AESCrypto.decryptCBC(encryptResult, aes256Key);
        assertEquals(testPlainText, decryptedText);
    }
    
    @Test
    @DisplayName("测试加密结果唯一性")
    void testEncryptionUniqueness() {
        // GCM模式
        var result1 = AESCrypto.encryptGCM(testPlainText, aes256Key);
        var result2 = AESCrypto.encryptGCM(testPlainText, aes256Key);
        
        // IV应该不同
        assertNotEquals(result1.ivHex(), result2.ivHex());
        // 密文应该不同
        assertNotEquals(result1.cipherTextHex(), result2.cipherTextHex());
        
        // 但都应该能正确解密
        assertEquals(testPlainText, AESCrypto.decryptGCM(result1, aes256Key));
        assertEquals(testPlainText, AESCrypto.decryptGCM(result2, aes256Key));
    }
    
    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        var validResult = AESCrypto.encryptGCM(testPlainText, aes256Key);
        
        // 测试无效密钥
        assertThrows(Exception.class, () -> 
            AESCrypto.decryptGCM(validResult, "invalid_key"));
        
        // 测试Record验证
        assertThrows(IllegalArgumentException.class, () -> 
            new AESCrypto.AESResult("", "test", "test", "GCM"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            new AESCrypto.AESResult("test", "", "test", "GCM"));
    }
}
