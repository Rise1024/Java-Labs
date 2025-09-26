package com.javalaabs.crypto.encoding;

import com.javalaabs.crypto.utils.CryptoException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * URL编码解码工具类
 * 支持标准URL编码、表单编码、路径编码等
 * 
 * @author JavaLabs
 */
@Slf4j
public final class UrlEncoder {
    
    // URL编码相关常量
    private static final String UTF8 = StandardCharsets.UTF_8.name();
    private static final Pattern ENCODED_PATTERN = Pattern.compile("%[0-9A-Fa-f]{2}");
    
    // 需要编码的特殊字符映射
    private static final Map<Character, String> SPECIAL_CHARS = new HashMap<>();
    static {
        SPECIAL_CHARS.put(' ', "%20");
        SPECIAL_CHARS.put('!', "%21");
        SPECIAL_CHARS.put('"', "%22");
        SPECIAL_CHARS.put('#', "%23");
        SPECIAL_CHARS.put('$', "%24");
        SPECIAL_CHARS.put('%', "%25");
        SPECIAL_CHARS.put('&', "%26");
        SPECIAL_CHARS.put('\'', "%27");
        SPECIAL_CHARS.put('(', "%28");
        SPECIAL_CHARS.put(')', "%29");
        SPECIAL_CHARS.put('*', "%2A");
        SPECIAL_CHARS.put('+', "%2B");
        SPECIAL_CHARS.put(',', "%2C");
        SPECIAL_CHARS.put('/', "%2F");
        SPECIAL_CHARS.put(':', "%3A");
        SPECIAL_CHARS.put(';', "%3B");
        SPECIAL_CHARS.put('=', "%3D");
        SPECIAL_CHARS.put('?', "%3F");
        SPECIAL_CHARS.put('@', "%40");
        SPECIAL_CHARS.put('[', "%5B");
        SPECIAL_CHARS.put(']', "%5D");
    }
    
    // 私有构造器
    private UrlEncoder() {
        throw new AssertionError("工具类不能被实例化");
    }
    
    /**
     * 标准URL编码（UTF-8）
     * 
     * @param text 待编码文本
     * @return 编码后的URL字符串
     */
    public static String encode(String text) {
        if (text == null) {
            return null;
        }
        
        try {
            String encoded = URLEncoder.encode(text, UTF8);
            log.debug("URL编码: {} -> {}", text, encoded);
            return encoded;
        } catch (UnsupportedEncodingException e) {
            log.error("URL编码失败", e);
            throw new CryptoException("URL编码失败", e);
        }
    }
    
    /**
     * 标准URL解码（UTF-8）
     * 
     * @param encodedText 编码后的URL字符串
     * @return 解码后的文本
     */
    public static String decode(String encodedText) {
        if (encodedText == null) {
            return null;
        }
        
        try {
            String decoded = URLDecoder.decode(encodedText, UTF8);
            log.debug("URL解码: {} -> {}", encodedText, decoded);
            return decoded;
        } catch (UnsupportedEncodingException e) {
            log.error("URL解码失败", e);
            throw new CryptoException("URL解码失败", e);
        }
    }
    
    /**
     * 表单数据编码（application/x-www-form-urlencoded）
     * 
     * @param text 待编码文本
     * @return 编码后的字符串
     */
    public static String encodeFormData(String text) {
        if (text == null) {
            return null;
        }
        
        // 表单编码将空格编码为+而不是%20
        String encoded = encode(text);
        String formEncoded = encoded.replace("%20", "+");
        
        log.debug("表单编码: {} -> {}", text, formEncoded);
        return formEncoded;
    }
    
    /**
     * 表单数据解码
     * 
     * @param encodedText 编码后的表单数据
     * @return 解码后的文本
     */
    public static String decodeFormData(String encodedText) {
        if (encodedText == null) {
            return null;
        }
        
        // 先将+号替换为%20，再进行标准解码
        String standardEncoded = encodedText.replace("+", "%20");
        String decoded = decode(standardEncoded);
        
        log.debug("表单解码: {} -> {}", encodedText, decoded);
        return decoded;
    }
    
    /**
     * 路径安全编码（仅编码必要字符，保留路径分隔符）
     * 
     * @param path 路径字符串
     * @return 编码后的路径
     */
    public static String encodePathSafe(String path) {
        if (path == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (char c : path.toCharArray()) {
            if (c == '/' || c == '-' || c == '_' || c == '.' || c == '~' ||
                (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                // 路径安全字符，不编码
                result.append(c);
            } else {
                // 需要编码的字符
                result.append(String.format("%%%02X", (int) c));
            }
        }
        
        String encoded = result.toString();
        log.debug("路径安全编码: {} -> {}", path, encoded);
        return encoded;
    }
    
    /**
     * 查询参数编码
     * 
     * @param params 参数映射
     * @return 编码后的查询字符串
     */
    public static String encodeQueryParams(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        
        StringBuilder query = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                query.append("&");
            }
            
            String key = entry.getKey();
            String value = entry.getValue();
            
            query.append(encode(key));
            if (value != null) {
                query.append("=").append(encode(value));
            }
            
            first = false;
        }
        
        String queryString = query.toString();
        log.debug("查询参数编码: {} 个参数 -> {}", params.size(), queryString);
        return queryString;
    }
    
    /**
     * 查询参数解码
     * 
     * @param queryString 查询字符串
     * @return 参数映射
     */
    public static Map<String, String> decodeQueryParams(String queryString) {
        Map<String, String> params = new HashMap<>();
        
        if (queryString == null || queryString.isEmpty()) {
            return params;
        }
        
        // 移除开头的?符号
        if (queryString.startsWith("?")) {
            queryString = queryString.substring(1);
        }
        
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            String key = decode(keyValue[0]);
            String value = keyValue.length > 1 ? decode(keyValue[1]) : null;
            params.put(key, value);
        }
        
        log.debug("查询参数解码: {} -> {} 个参数", queryString, params.size());
        return params;
    }
    
    /**
     * 检查字符串是否已经URL编码
     * 
     * @param text 待检查的字符串
     * @return 是否已编码
     */
    public static boolean isUrlEncoded(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        
        // 检查是否包含%XX格式的编码
        boolean hasEncoding = ENCODED_PATTERN.matcher(text).find();
        
        // 如果包含编码，尝试解码后再编码，看是否一致
        if (hasEncoding) {
            try {
                String decoded = decode(text);
                String reencoded = encode(decoded);
                return text.equals(reencoded);
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }
    
    /**
     * 获取字符的URL编码
     * 
     * @param c 字符
     * @return URL编码字符串
     */
    public static String getCharEncoding(char c) {
        if (SPECIAL_CHARS.containsKey(c)) {
            return SPECIAL_CHARS.get(c);
        }
        
        // 对于ASCII可打印字符，不编码
        if (c >= 32 && c <= 126 && c != '%') {
            return String.valueOf(c);
        }
        
        // 其他字符进行百分号编码
        return String.format("%%%02X", (int) c);
    }
    
    /**
     * 完整URL编码示例
     * 
     * @param url 原始URL
     * @return URL编码结果
     */
    public static UrlEncodingResult encodeFullUrl(String url) {
        if (url == null) {
            throw new IllegalArgumentException("URL不能为空");
        }
        
        try {
            // 分解URL组件
            String[] parts = url.split("\\?", 2);
            String basePath = parts[0];
            String queryString = parts.length > 1 ? parts[1] : "";
            
            // 编码路径部分
            String encodedPath = encodePathSafe(basePath);
            
            // 编码查询参数
            Map<String, String> params = decodeQueryParams(queryString);
            String encodedQuery = encodeQueryParams(params);
            
            // 组合完整URL
            String fullEncoded = encodedPath + (encodedQuery.isEmpty() ? "" : "?" + encodedQuery);
            
            return new UrlEncodingResult(
                url,
                fullEncoded,
                encodedPath,
                encodedQuery,
                params.size()
            );
            
        } catch (Exception e) {
            log.error("完整URL编码失败: {}", url, e);
            throw new CryptoException("URL编码失败", e);
        }
    }
    
    /**
     * URL编码结果记录
     * 
     * @param originalUrl 原始URL
     * @param encodedUrl 编码后的URL
     * @param encodedPath 编码后的路径
     * @param encodedQuery 编码后的查询字符串
     * @param paramCount 参数数量
     */
    public record UrlEncodingResult(
        String originalUrl,
        String encodedUrl,
        String encodedPath,
        String encodedQuery,
        int paramCount
    ) {
        
        /**
         * 转换为JSON格式
         * 
         * @return JSON字符串
         */
        public String toJson() {
            return """
                {
                    "originalUrl": "%s",
                    "encodedUrl": "%s",
                    "encodedPath": "%s",
                    "encodedQuery": "%s",
                    "paramCount": %d
                }
                """.formatted(originalUrl, encodedUrl, encodedPath, encodedQuery, paramCount);
        }
        
        /**
         * 转换为详细格式
         * 
         * @return 详细信息字符串
         */
        public String toDetailedString() {
            return """
                URL编码结果:
                ├─ 原始URL: %s
                ├─ 编码URL: %s
                ├─ 编码路径: %s
                ├─ 编码查询: %s
                └─ 参数数量: %d
                """.formatted(originalUrl, encodedUrl, encodedPath, encodedQuery, paramCount);
        }
    }
}
