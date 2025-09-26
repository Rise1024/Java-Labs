package com.javalaabs.crypto.encoding;

import com.javalaabs.crypto.utils.CryptoException;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * 进制转换工具类
 * 支持2-36进制之间的相互转换
 * 
 * @author JavaLabs
 */
@Slf4j
public final class BaseConverter {
    
    // 常用进制常量
    public static final int BINARY = 2;
    public static final int OCTAL = 8;
    public static final int DECIMAL = 10;
    public static final int HEXADECIMAL = 16;
    
    // 自定义进制字符集
    private static final String BASE36_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
    // 私有构造器
    private BaseConverter() {
        throw new AssertionError("工具类不能被实例化");
    }
    
    /**
     * 将十进制数转换为指定进制
     * 
     * @param decimal 十进制数
     * @param targetBase 目标进制（2-36）
     * @return 目标进制字符串
     */
    public static String decimalToBase(long decimal, int targetBase) {
        validateBase(targetBase);
        
        if (decimal == 0) {
            return "0";
        }
        
        boolean negative = decimal < 0;
        decimal = Math.abs(decimal);
        
        StringBuilder result = new StringBuilder();
        while (decimal > 0) {
            int remainder = (int) (decimal % targetBase);
            result.insert(0, BASE36_CHARS.charAt(remainder));
            decimal /= targetBase;
        }
        
        if (negative) {
            result.insert(0, '-');
        }
        
        String converted = result.toString();
        log.debug("十进制 {} 转换为 {}-进制: {}", negative ? -Math.abs(decimal) : decimal, targetBase, converted);
        return converted;
    }
    
    /**
     * 将指定进制转换为十进制
     * 
     * @param value 源进制字符串
     * @param sourceBase 源进制（2-36）
     * @return 十进制数
     */
    public static long baseToDecimal(String value, int sourceBase) {
        validateBase(sourceBase);
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("输入值不能为空");
        }
        
        boolean negative = value.startsWith("-");
        if (negative) {
            value = value.substring(1);
        }
        
        long result = 0;
        long power = 1;
        
        for (int i = value.length() - 1; i >= 0; i--) {
            char digit = value.charAt(i);
            int digitValue = getDigitValue(digit, sourceBase);
            result += digitValue * power;
            power *= sourceBase;
        }
        
        long finalResult = negative ? -result : result;
        log.debug("{}-进制 {} 转换为十进制: {}", sourceBase, value, finalResult);
        return finalResult;
    }
    
    /**
     * 任意进制之间的转换
     * 
     * @param value 源进制字符串
     * @param sourceBase 源进制
     * @param targetBase 目标进制
     * @return 目标进制字符串
     */
    public static String convertBase(String value, int sourceBase, int targetBase) {
        // 先转为十进制，再转为目标进制
        long decimal = baseToDecimal(value, sourceBase);
        return decimalToBase(decimal, targetBase);
    }
    
    /**
     * 大数进制转换（支持超长数字）
     * 
     * @param value 源进制字符串
     * @param sourceBase 源进制
     * @param targetBase 目标进制
     * @return 目标进制字符串
     */
    public static String convertBaseBigInteger(String value, int sourceBase, int targetBase) {
        validateBase(sourceBase);
        validateBase(targetBase);
        
        try {
            // 使用BigInteger处理大数
            BigInteger bigInteger = new BigInteger(value, sourceBase);
            String result = bigInteger.toString(targetBase).toUpperCase();
            
            log.debug("大数{}-进制 {} 转换为{}-进制: {}", sourceBase, value, targetBase, result);
            return result;
            
        } catch (NumberFormatException e) {
            throw new CryptoException("无效的进制数字: " + value, e);
        }
    }
    
    /**
     * 二进制转十进制
     * 
     * @param binary 二进制字符串
     * @return 十进制数
     */
    public static long binaryToDecimal(String binary) {
        return baseToDecimal(binary, BINARY);
    }
    
    /**
     * 十进制转二进制
     * 
     * @param decimal 十进制数
     * @return 二进制字符串
     */
    public static String decimalToBinary(long decimal) {
        return decimalToBase(decimal, BINARY);
    }
    
    /**
     * 八进制转十进制
     * 
     * @param octal 八进制字符串
     * @return 十进制数
     */
    public static long octalToDecimal(String octal) {
        return baseToDecimal(octal, OCTAL);
    }
    
    /**
     * 十进制转八进制
     * 
     * @param decimal 十进制数
     * @return 八进制字符串
     */
    public static String decimalToOctal(long decimal) {
        return decimalToBase(decimal, OCTAL);
    }
    
    /**
     * 十六进制转十进制
     * 
     * @param hex 十六进制字符串
     * @return 十进制数
     */
    public static long hexToDecimal(String hex) {
        return baseToDecimal(hex, HEXADECIMAL);
    }
    
    /**
     * 十进制转十六进制
     * 
     * @param decimal 十进制数
     * @return 十六进制字符串
     */
    public static String decimalToHex(long decimal) {
        return decimalToBase(decimal, HEXADECIMAL);
    }
    
    /**
     * Base62编码（常用于短链接）
     * 
     * @param number 数字
     * @return Base62字符串
     */
    public static String encodeBase62(long number) {
        if (number == 0) {
            return "0";
        }
        
        boolean negative = number < 0;
        number = Math.abs(number);
        
        StringBuilder result = new StringBuilder();
        while (number > 0) {
            result.insert(0, BASE62_CHARS.charAt((int) (number % 62)));
            number /= 62;
        }
        
        if (negative) {
            result.insert(0, '-');
        }
        
        String encoded = result.toString();
        log.debug("数字 {} 编码为 Base62: {}", negative ? -Math.abs(number) : number, encoded);
        return encoded;
    }
    
    /**
     * Base62解码
     * 
     * @param base62 Base62字符串
     * @return 数字
     */
    public static long decodeBase62(String base62) {
        if (base62 == null || base62.isEmpty()) {
            throw new IllegalArgumentException("Base62字符串不能为空");
        }
        
        boolean negative = base62.startsWith("-");
        if (negative) {
            base62 = base62.substring(1);
        }
        
        long result = 0;
        long power = 1;
        
        for (int i = base62.length() - 1; i >= 0; i--) {
            char c = base62.charAt(i);
            int index = BASE62_CHARS.indexOf(c);
            if (index == -1) {
                throw new IllegalArgumentException("无效的Base62字符: " + c);
            }
            result += index * power;
            power *= 62;
        }
        
        long finalResult = negative ? -result : result;
        log.debug("Base62 {} 解码为数字: {}", base62, finalResult);
        return finalResult;
    }
    
    /**
     * 获取进制转换结果详情
     * 
     * @param decimal 十进制数
     * @return 转换结果详情
     */
    public static BaseConversionResult getAllBaseConversions(long decimal) {
        return new BaseConversionResult(
            decimal,
            decimalToBinary(decimal),
            decimalToOctal(decimal),
            decimalToHex(decimal),
            decimalToBase(decimal, 32),
            encodeBase62(decimal)
        );
    }
    
    /**
     * 验证进制是否有效
     * 
     * @param base 进制
     */
    private static void validateBase(int base) {
        if (base < 2 || base > 36) {
            throw new IllegalArgumentException("进制必须在2-36之间，当前: " + base);
        }
    }
    
    /**
     * 获取字符的数字值
     * 
     * @param digit 字符
     * @param base 进制
     * @return 数字值
     */
    private static int getDigitValue(char digit, int base) {
        int value;
        if (digit >= '0' && digit <= '9') {
            value = digit - '0';
        } else if (digit >= 'A' && digit <= 'Z') {
            value = digit - 'A' + 10;
        } else if (digit >= 'a' && digit <= 'z') {
            value = digit - 'a' + 10;
        } else {
            throw new IllegalArgumentException("无效的数字字符: " + digit);
        }
        
        if (value >= base) {
            throw new IllegalArgumentException(
                String.format("字符 '%c' 不适用于 %d 进制", digit, base));
        }
        
        return value;
    }
    
    /**
     * 进制转换结果记录
     * 
     * @param decimal 十进制
     * @param binary 二进制
     * @param octal 八进制
     * @param hexadecimal 十六进制
     * @param base32 32进制
     * @param base62 Base62
     */
    public record BaseConversionResult(
        long decimal,
        String binary,
        String octal,
        String hexadecimal,
        String base32,
        String base62
    ) {
        
        /**
         * 转换为JSON格式
         * 
         * @return JSON字符串
         */
        public String toJson() {
            return """
                {
                    "decimal": %d,
                    "binary": "%s",
                    "octal": "%s",
                    "hexadecimal": "%s",
                    "base32": "%s",
                    "base62": "%s"
                }
                """.formatted(decimal, binary, octal, hexadecimal, base32, base62);
        }
        
        /**
         * 转换为表格格式
         * 
         * @return 表格字符串
         */
        public String toTable() {
            return """
                ┌──────────────┬─────────────────────────────────────┐
                │   进制类型   │                 值                  │
                ├──────────────┼─────────────────────────────────────┤
                │ 十进制 (10)  │ %-35d │
                │ 二进制 (2)   │ %-35s │
                │ 八进制 (8)   │ %-35s │
                │ 十六进制(16) │ %-35s │
                │ 32进制       │ %-35s │
                │ Base62       │ %-35s │
                └──────────────┴─────────────────────────────────────┘
                """.formatted(decimal, binary, octal, hexadecimal, base32, base62);
        }
    }
}
