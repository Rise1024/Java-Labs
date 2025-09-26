package com.javalaabs.crypto.password;

import com.javalaabs.crypto.utils.CryptoException;
import com.javalaabs.crypto.utils.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;

/**
 * 密码哈希算法实现
 * 包括 PBKDF2 和 BCrypt
 * 
 * @author JavaLabs
 */
@Slf4j
public class PasswordHashCrypto {
    
    // PBKDF2算法常量
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_PBKDF2_ITERATIONS = 100_000;
    private static final int DEFAULT_SALT_LENGTH = 16;
    private static final int DEFAULT_HASH_LENGTH = 32;
    
    // BCrypt算法常量
    private static final int DEFAULT_BCRYPT_ROUNDS = 12;
    private static final int MIN_BCRYPT_ROUNDS = 4;
    private static final int MAX_BCRYPT_ROUNDS = 31;
    
    /**
     * 生成安全的盐值
     * 
     * @param length 盐值长度（字节）
     * @return Base64编码的盐值
     */
    public static String generateSalt(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("盐值长度必须大于0");
        }
        
        byte[] salt = CryptoUtils.generateSecureRandomBytes(length);
        String saltBase64 = CryptoUtils.bytesToBase64(salt);
        
        log.info("生成{}字节盐值成功", length);
        return saltBase64;
    }
    
    /**
     * PBKDF2密码哈希（推荐）
     * 
     * @param password 原始密码
     * @param saltBase64 Base64编码的盐值
     * @param iterations 迭代次数（推荐100,000+）
     * @param hashLength 哈希长度（字节）
     * @return 密码哈希结果
     */
    public static PasswordHashResult pbkdf2Hash(String password, String saltBase64, int iterations, int hashLength) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (iterations < 10_000) {
            log.warn("PBKDF2迭代次数过低，建议至少100,000次");
        }
        if (hashLength < 16) {
            throw new IllegalArgumentException("哈希长度至少16字节");
        }
        
        try {
            // 解析盐值
            byte[] salt = CryptoUtils.base64ToBytes(saltBase64);
            
            // 创建PBKDF2规范
            PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), 
                salt, 
                iterations, 
                hashLength * 8 // 转换为位长度
            );
            
            // 执行哈希
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            
            // 清除敏感数据
            spec.clearPassword();
            CryptoUtils.clearSensitiveData(password.toCharArray());
            
            log.info("PBKDF2密码哈希完成 - 迭代次数: {}, 哈希长度: {} bytes", iterations, hash.length);
            
            return new PasswordHashResult(
                CryptoUtils.bytesToHex(hash),
                CryptoUtils.bytesToBase64(hash),
                saltBase64,
                PBKDF2_ALGORITHM,
                iterations,
                hash.length * 8
            );
            
        } catch (Exception e) {
            log.error("PBKDF2密码哈希失败", e);
            throw new CryptoException("PBKDF2密码哈希失败", e);
        }
    }
    
    /**
     * PBKDF2密码哈希（使用默认参数）
     * 
     * @param password 原始密码
     * @return 密码哈希结果
     */
    public static PasswordHashResult pbkdf2Hash(String password) {
        String salt = generateSalt(DEFAULT_SALT_LENGTH);
        return pbkdf2Hash(password, salt, DEFAULT_PBKDF2_ITERATIONS, DEFAULT_HASH_LENGTH);
    }
    
    /**
     * 验证PBKDF2密码
     * 
     * @param password 待验证的密码
     * @param hashResult 原始哈希结果
     * @return 是否匹配
     */
    public static boolean verifyPBKDF2Password(String password, PasswordHashResult hashResult) {
        try {
            // 使用相同参数重新哈希
            PasswordHashResult newHash = pbkdf2Hash(
                password, 
                hashResult.saltBase64(), 
                hashResult.iterations(), 
                hashResult.bitLength() / 8
            );
            
            // 安全比较
            boolean matches = CryptoUtils.secureEquals(
                CryptoUtils.hexToBytes(hashResult.hexHash()),
                CryptoUtils.hexToBytes(newHash.hexHash())
            );
            
            log.info("PBKDF2密码验证结果: {}", matches ? "匹配" : "不匹配");
            return matches;
            
        } catch (Exception e) {
            log.error("PBKDF2密码验证失败", e);
            return false;
        }
    }
    
    /**
     * BCrypt密码哈希
     * 
     * @param password 原始密码
     * @param rounds 轮数（4-31，推荐12+）
     * @return BCrypt哈希结果
     */
    public static BCryptResult bcryptHash(String password, int rounds) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (rounds < MIN_BCRYPT_ROUNDS || rounds > MAX_BCRYPT_ROUNDS) {
            throw new IllegalArgumentException(
                String.format("BCrypt轮数必须在%d-%d之间", MIN_BCRYPT_ROUNDS, MAX_BCRYPT_ROUNDS));
        }
        
        try {
            // 生成盐值并执行哈希
            String salt = BCrypt.gensalt(rounds);
            String hash = BCrypt.hashpw(password, salt);
            
            log.info("BCrypt密码哈希完成 - 轮数: {}", rounds);
            
            return new BCryptResult(hash, salt, rounds);
            
        } catch (Exception e) {
            log.error("BCrypt密码哈希失败", e);
            throw new CryptoException("BCrypt密码哈希失败", e);
        }
    }
    
    /**
     * BCrypt密码哈希（使用默认轮数）
     * 
     * @param password 原始密码
     * @return BCrypt哈希结果
     */
    public static BCryptResult bcryptHash(String password) {
        return bcryptHash(password, DEFAULT_BCRYPT_ROUNDS);
    }
    
    /**
     * 验证BCrypt密码
     * 
     * @param password 待验证的密码
     * @param hash BCrypt哈希值
     * @return 是否匹配
     */
    public static boolean verifyBCryptPassword(String password, String hash) {
        try {
            boolean matches = BCrypt.checkpw(password, hash);
            log.info("BCrypt密码验证结果: {}", matches ? "匹配" : "不匹配");
            return matches;
            
        } catch (Exception e) {
            log.error("BCrypt密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 生成安全的随机密码
     * 
     * @param length 密码长度
     * @param includeUppercase 包含大写字母
     * @param includeLowercase 包含小写字母
     * @param includeNumbers 包含数字
     * @param includeSpecialChars 包含特殊字符
     * @return 随机密码
     */
    public static String generateSecurePassword(int length, boolean includeUppercase, 
                                              boolean includeLowercase, boolean includeNumbers, 
                                              boolean includeSpecialChars) {
        if (length <= 0) {
            throw new IllegalArgumentException("密码长度必须大于0");
        }
        
        StringBuilder charset = new StringBuilder();
        if (includeUppercase) charset.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        if (includeLowercase) charset.append("abcdefghijklmnopqrstuvwxyz");
        if (includeNumbers) charset.append("0123456789");
        if (includeSpecialChars) charset.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        
        if (charset.length() == 0) {
            throw new IllegalArgumentException("至少选择一种字符类型");
        }
        
        try {
            SecureRandom random = SecureRandom.getInstanceStrong();
            StringBuilder password = new StringBuilder(length);
            
            for (int i = 0; i < length; i++) {
                int index = random.nextInt(charset.length());
                password.append(charset.charAt(index));
            }
            
            log.info("生成{}位安全密码成功", length);
            return password.toString();
            
        } catch (Exception e) {
            log.error("生成安全密码失败", e);
            throw new CryptoException("生成安全密码失败", e);
        }
    }
    
    /**
     * 密码哈希结果记录（PBKDF2）
     * 
     * @param hexHash 十六进制哈希
     * @param base64Hash Base64哈希
     * @param saltBase64 Base64盐值
     * @param algorithm 算法名称
     * @param iterations 迭代次数
     * @param bitLength 哈希位长度
     */
    public record PasswordHashResult(String hexHash, String base64Hash, String saltBase64, 
                                   String algorithm, int iterations, int bitLength) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public PasswordHashResult {
            if (hexHash == null || hexHash.isEmpty()) {
                throw new IllegalArgumentException("哈希值不能为空");
            }
            if (saltBase64 == null || saltBase64.isEmpty()) {
                throw new IllegalArgumentException("盐值不能为空");
            }
            if (algorithm == null || algorithm.isEmpty()) {
                throw new IllegalArgumentException("算法名称不能为空");
            }
            if (iterations <= 0) {
                throw new IllegalArgumentException("迭代次数必须大于0");
            }
            if (bitLength <= 0) {
                throw new IllegalArgumentException("哈希位长度必须大于0");
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
                    "iterations": %d,
                    "bitLength": %d,
                    "salt": "%s",
                    "hash": "%s"
                }
                """.formatted(algorithm, iterations, bitLength, saltBase64, base64Hash);
        }
    }
    
    /**
     * BCrypt哈希结果记录
     * 
     * @param hash BCrypt哈希值
     * @param salt 盐值
     * @param rounds 轮数
     */
    public record BCryptResult(String hash, String salt, int rounds) {
        
        /**
         * 紧凑构造器 - 验证参数
         */
        public BCryptResult {
            if (hash == null || hash.isEmpty()) {
                throw new IllegalArgumentException("哈希值不能为空");
            }
            if (salt == null || salt.isEmpty()) {
                throw new IllegalArgumentException("盐值不能为空");
            }
            if (rounds < MIN_BCRYPT_ROUNDS || rounds > MAX_BCRYPT_ROUNDS) {
                throw new IllegalArgumentException("轮数必须在" + MIN_BCRYPT_ROUNDS + "-" + MAX_BCRYPT_ROUNDS + "之间");
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
                    "algorithm": "BCrypt",
                    "rounds": %d,
                    "salt": "%s",
                    "hash": "%s"
                }
                """.formatted(rounds, salt, hash);
        }
    }
}
