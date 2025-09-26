package com.javalaabs.crypto.encoding;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 进制转换测试
 * 
 * @author JavaLabs
 */
class BaseConverterTest {
    
    @Test
    @DisplayName("测试十进制转二进制")
    void testDecimalToBinary() {
        assertEquals("1010", BaseConverter.decimalToBinary(10));
        assertEquals("1111", BaseConverter.decimalToBinary(15));
        assertEquals("0", BaseConverter.decimalToBinary(0));
        assertEquals("-1010", BaseConverter.decimalToBinary(-10));
    }
    
    @Test
    @DisplayName("测试二进制转十进制")
    void testBinaryToDecimal() {
        assertEquals(10, BaseConverter.binaryToDecimal("1010"));
        assertEquals(15, BaseConverter.binaryToDecimal("1111"));
        assertEquals(0, BaseConverter.binaryToDecimal("0"));
    }
    
    @Test
    @DisplayName("测试十进制转十六进制")
    void testDecimalToHex() {
        assertEquals("A", BaseConverter.decimalToHex(10));
        assertEquals("FF", BaseConverter.decimalToHex(255));
        assertEquals("100", BaseConverter.decimalToHex(256));
    }
    
    @Test
    @DisplayName("测试十六进制转十进制")
    void testHexToDecimal() {
        assertEquals(10, BaseConverter.hexToDecimal("A"));
        assertEquals(255, BaseConverter.hexToDecimal("FF"));
        assertEquals(256, BaseConverter.hexToDecimal("100"));
    }
    
    @Test
    @DisplayName("测试任意进制转换")
    void testConvertBase() {
        // 二进制转十六进制
        assertEquals("A", BaseConverter.convertBase("1010", 2, 16));
        // 八进制转十进制
        assertEquals("8", BaseConverter.convertBase("10", 8, 10));
        // 十进制转三十六进制
        assertEquals("Z", BaseConverter.convertBase("35", 10, 36));
    }
    
    @Test
    @DisplayName("测试Base62编码")
    void testBase62() {
        long number = 12345;
        String encoded = BaseConverter.encodeBase62(number);
        long decoded = BaseConverter.decodeBase62(encoded);
        
        assertEquals(number, decoded);
        assertNotNull(encoded);
        assertTrue(encoded.length() > 0);
    }
    
    @Test
    @DisplayName("测试大数进制转换")
    void testBigIntegerConversion() {
        String largeHex = "FFFFFFFFFF";
        String binary = BaseConverter.convertBaseBigInteger(largeHex, 16, 2);
        String backToHex = BaseConverter.convertBaseBigInteger(binary, 2, 16);
        
        assertEquals(largeHex, backToHex);
    }
    
    @Test
    @DisplayName("测试获取所有进制转换")
    void testGetAllBaseConversions() {
        long decimal = 255;
        var result = BaseConverter.getAllBaseConversions(decimal);
        
        assertEquals(decimal, result.decimal());
        assertEquals("11111111", result.binary());
        assertEquals("377", result.octal());
        assertEquals("FF", result.hexadecimal());
        assertNotNull(result.base32());
        assertNotNull(result.base62());
    }
    
    @Test
    @DisplayName("测试错误处理")
    void testErrorHandling() {
        // 无效进制
        assertThrows(IllegalArgumentException.class, () -> 
            BaseConverter.decimalToBase(10, 1));
        
        assertThrows(IllegalArgumentException.class, () -> 
            BaseConverter.baseToDecimal("10", 37));
        
        // 无效字符
        assertThrows(IllegalArgumentException.class, () -> 
            BaseConverter.baseToDecimal("Z", 10));
        
        // 空字符串
        assertThrows(IllegalArgumentException.class, () -> 
            BaseConverter.baseToDecimal("", 10));
    }
}
