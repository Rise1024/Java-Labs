package com.javalaabs.crypto.encoding;

import com.javalaabs.crypto.utils.CryptoException;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.stream.Collectors;

/**
 * 文本编码工具类
 * 支持十六进制、Unicode、ASCII等编码
 * 
 * @author JavaLabs
 */
@Slf4j
public final class TextEncoder {
    
    // JDK17 HexFormat实例
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();
    private static final HexFormat HEX_FORMAT_LOWER = HexFormat.of().withLowerCase();
    
    // 私有构造器
    private TextEncoder() {
        throw new AssertionError("工具类不能被实例化");
    }
    
    /**
     * 文本转十六进制（大写）
     * 
     * @param text 输入文本
     * @return 十六进制字符串
     */
    public static String toHexUpper(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String hex = HEX_FORMAT.formatHex(bytes);
        
        log.debug("文本转十六进制(大写): {} -> {}", text, hex);
        return hex;
    }
    
    /**
     * 文本转十六进制（小写）
     * 
     * @param text 输入文本
     * @return 十六进制字符串
     */
    public static String toHexLower(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        String hex = HEX_FORMAT_LOWER.formatHex(bytes);
        
        log.debug("文本转十六进制(小写): {} -> {}", text, hex);
        return hex;
    }
    
    /**
     * 十六进制转文本
     * 
     * @param hex 十六进制字符串
     * @return 文本
     */
    public static String fromHex(String hex) {
        if (hex == null) {
            return null;
        }
        
        try {
            byte[] bytes = HEX_FORMAT.parseHex(hex);
            String text = new String(bytes, StandardCharsets.UTF_8);
            
            log.debug("十六进制转文本: {} -> {}", hex, text);
            return text;
        } catch (IllegalArgumentException e) {
            log.error("十六进制解码失败: {}", hex, e);
            throw new CryptoException("无效的十六进制字符串", e);
        }
    }
    
    /**
     * 文本转Unicode编码（\\uXXXX格式）
     * 
     * @param text 输入文本
     * @return Unicode编码字符串
     */
    public static String toUnicode(String text) {
        if (text == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c < 128) {
                // ASCII字符直接保留
                result.append(c);
            } else {
                // 非ASCII字符转为\\uXXXX格式
                result.append(String.format("\\u%04X", (int) c));
            }
        }
        
        String unicode = result.toString();
        log.debug("文本转Unicode: {} -> {}", text, unicode);
        return unicode;
    }
    
    /**
     * Unicode解码（\\uXXXX格式）
     * 
     * @param unicode Unicode编码字符串
     * @return 解码后的文本
     */
    public static String fromUnicode(String unicode) {
        if (unicode == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        int i = 0;
        
        while (i < unicode.length()) {
            if (i < unicode.length() - 5 && unicode.charAt(i) == '\\' && unicode.charAt(i + 1) == 'u') {
                // 处理\\uXXXX格式
                try {
                    String hexCode = unicode.substring(i + 2, i + 6);
                    int codePoint = Integer.parseInt(hexCode, 16);
                    result.append((char) codePoint);
                    i += 6;
                } catch (NumberFormatException e) {
                    // 无效的Unicode编码，保留原字符
                    result.append(unicode.charAt(i));
                    i++;
                }
            } else {
                result.append(unicode.charAt(i));
                i++;
            }
        }
        
        String text = result.toString();
        log.debug("Unicode解码: {} -> {}", unicode, text);
        return text;
    }
    
    /**
     * 文本转ASCII编码（每个字符的ASCII值）
     * 
     * @param text 输入文本
     * @return ASCII编码字符串
     */
    public static String toAscii(String text) {
        if (text == null) {
            return null;
        }
        
        String ascii = text.chars()
                          .mapToObj(c -> String.valueOf(c))
                          .collect(Collectors.joining(" "));
        
        log.debug("文本转ASCII: {} -> {}", text, ascii);
        return ascii;
    }
    
    /**
     * ASCII编码转文本
     * 
     * @param ascii ASCII编码字符串（空格分隔）
     * @return 解码后的文本
     */
    public static String fromAscii(String ascii) {
        if (ascii == null) {
            return null;
        }
        
        try {
            String[] codes = ascii.split("\\s+");
            StringBuilder result = new StringBuilder();
            
            for (String code : codes) {
                if (!code.isEmpty()) {
                    int charCode = Integer.parseInt(code);
                    result.append((char) charCode);
                }
            }
            
            String text = result.toString();
            log.debug("ASCII解码: {} -> {}", ascii, text);
            return text;
        } catch (NumberFormatException e) {
            log.error("ASCII解码失败: {}", ascii, e);
            throw new CryptoException("无效的ASCII编码", e);
        }
    }
    
    /**
     * 文本转二进制编码
     * 
     * @param text 输入文本
     * @return 二进制字符串
     */
    public static String toBinary(String text) {
        if (text == null) {
            return null;
        }
        
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        
        for (byte b : bytes) {
            String binary = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
            result.append(binary).append(" ");
        }
        
        String binaryStr = result.toString().trim();
        log.debug("文本转二进制: {} -> {}", text, binaryStr);
        return binaryStr;
    }
    
    /**
     * 二进制编码转文本
     * 
     * @param binary 二进制字符串（空格分隔）
     * @return 解码后的文本
     */
    public static String fromBinary(String binary) {
        if (binary == null) {
            return null;
        }
        
        try {
            String[] binaryBytes = binary.split("\\s+");
            byte[] bytes = new byte[binaryBytes.length];
            
            for (int i = 0; i < binaryBytes.length; i++) {
                bytes[i] = (byte) Integer.parseInt(binaryBytes[i], 2);
            }
            
            String text = new String(bytes, StandardCharsets.UTF_8);
            log.debug("二进制解码: {} -> {}", binary, text);
            return text;
        } catch (NumberFormatException e) {
            log.error("二进制解码失败: {}", binary, e);
            throw new CryptoException("无效的二进制编码", e);
        }
    }
    
    /**
     * HTML实体编码
     * 
     * @param text 输入文本
     * @return HTML实体编码字符串
     */
    public static String toHtmlEntities(String text) {
        if (text == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '<' -> result.append("&lt;");
                case '>' -> result.append("&gt;");
                case '&' -> result.append("&amp;");
                case '"' -> result.append("&quot;");
                case '\'' -> result.append("&#39;");
                default -> {
                    if (c > 127) {
                        result.append("&#").append((int) c).append(";");
                    } else {
                        result.append(c);
                    }
                }
            }
        }
        
        String htmlEntities = result.toString();
        log.debug("HTML实体编码: {} -> {}", text, htmlEntities);
        return htmlEntities;
    }
    
    /**
     * HTML实体解码
     * 
     * @param htmlEntities HTML实体编码字符串
     * @return 解码后的文本
     */
    public static String fromHtmlEntities(String htmlEntities) {
        if (htmlEntities == null) {
            return null;
        }
        
        String text = htmlEntities
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'");
        
        // 处理数字字符实体 &#数字; 使用Pattern和Matcher
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("&#(\\d+);");
        java.util.regex.Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String number = matcher.group(1);
            int charCode = Integer.parseInt(number);
            matcher.appendReplacement(sb, String.valueOf((char) charCode));
        }
        matcher.appendTail(sb);
        text = sb.toString();
        
        log.debug("HTML实体解码: {} -> {}", htmlEntities, text);
        return text;
    }
    
    /**
     * 获取文本的各种编码格式
     * 
     * @param text 输入文本
     * @return 编码结果
     */
    public static TextEncodingResult getAllEncodings(String text) {
        if (text == null) {
            throw new IllegalArgumentException("输入文本不能为空");
        }
        
        return new TextEncodingResult(
            text,
            toHexUpper(text),
            toHexLower(text),
            toUnicode(text),
            toAscii(text),
            toBinary(text),
            toHtmlEntities(text)
        );
    }
    
    /**
     * 检测编码类型
     * 
     * @param encoded 编码字符串
     * @return 检测结果
     */
    public static EncodingDetectionResult detectEncoding(String encoded) {
        if (encoded == null) {
            throw new IllegalArgumentException("编码字符串不能为空");
        }
        
        boolean isHex = isValidHex(encoded);
        boolean isUnicode = encoded.contains("\\u");
        boolean isAscii = isValidAscii(encoded);
        boolean isBinary = isValidBinary(encoded);
        boolean isHtmlEntities = encoded.contains("&") && encoded.contains(";");
        
        return new EncodingDetectionResult(
            encoded,
            isHex,
            isUnicode,
            isAscii,
            isBinary,
            isHtmlEntities
        );
    }
    
    /**
     * 检查是否为有效十六进制
     */
    private static boolean isValidHex(String text) {
        return text.matches("^[0-9A-Fa-f]+$");
    }
    
    /**
     * 检查是否为有效ASCII编码
     */
    private static boolean isValidAscii(String text) {
        try {
            String[] parts = text.split("\\s+");
            for (String part : parts) {
                int code = Integer.parseInt(part);
                if (code < 0 || code > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 检查是否为有效二进制编码
     */
    private static boolean isValidBinary(String text) {
        return text.matches("^[01\\s]+$");
    }
    
    /**
     * 文本编码结果记录
     * 
     * @param originalText 原始文本
     * @param hexUpper 大写十六进制
     * @param hexLower 小写十六进制
     * @param unicode Unicode编码
     * @param ascii ASCII编码
     * @param binary 二进制编码
     * @param htmlEntities HTML实体编码
     */
    public record TextEncodingResult(
        String originalText,
        String hexUpper,
        String hexLower,
        String unicode,
        String ascii,
        String binary,
        String htmlEntities
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
                    "hexUpper": "%s",
                    "hexLower": "%s",
                    "unicode": "%s",
                    "ascii": "%s",
                    "binary": "%s",
                    "htmlEntities": "%s"
                }
                """.formatted(originalText, hexUpper, hexLower, unicode, ascii, 
                              binary.replace(" ", ""), htmlEntities);
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
                │ 十六进制(大写)  │ %-35s │
                │ 十六进制(小写)  │ %-35s │
                │ Unicode编码     │ %-35s │
                │ ASCII编码       │ %-35s │
                │ 二进制编码      │ %-35s │
                │ HTML实体编码    │ %-35s │
                └─────────────────┴─────────────────────────────────────┘
                """.formatted(originalText, 
                              hexUpper.length() > 35 ? hexUpper.substring(0, 32) + "..." : hexUpper,
                              hexLower.length() > 35 ? hexLower.substring(0, 32) + "..." : hexLower,
                              unicode.length() > 35 ? unicode.substring(0, 32) + "..." : unicode,
                              ascii.length() > 35 ? ascii.substring(0, 32) + "..." : ascii,
                              binary.length() > 35 ? binary.substring(0, 32) + "..." : binary,
                              htmlEntities.length() > 35 ? htmlEntities.substring(0, 32) + "..." : htmlEntities);
        }
    }
    
    /**
     * 编码检测结果记录
     * 
     * @param encoded 编码字符串
     * @param isHex 是否为十六进制
     * @param isUnicode 是否为Unicode
     * @param isAscii 是否为ASCII
     * @param isBinary 是否为二进制
     * @param isHtmlEntities 是否为HTML实体
     */
    public record EncodingDetectionResult(
        String encoded,
        boolean isHex,
        boolean isUnicode,
        boolean isAscii,
        boolean isBinary,
        boolean isHtmlEntities
    ) {
        
        /**
         * 转换为检测报告
         * 
         * @return 检测报告字符串
         */
        public String toDetectionReport() {
            return """
                编码类型检测报告:
                ├─ 输入字符串: %s
                ├─ 十六进制: %s
                ├─ Unicode: %s
                ├─ ASCII: %s
                ├─ 二进制: %s
                └─ HTML实体: %s
                """.formatted(encoded.length() > 50 ? encoded.substring(0, 47) + "..." : encoded,
                             isHex ? "✓ 可能" : "✗ 否",
                             isUnicode ? "✓ 可能" : "✗ 否",
                             isAscii ? "✓ 可能" : "✗ 否",
                             isBinary ? "✓ 可能" : "✗ 否",
                             isHtmlEntities ? "✓ 可能" : "✗ 否");
        }
    }
}
