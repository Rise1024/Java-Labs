# Java 加密算法学习项目

基于 JDK 17 的现代 Java 加密算法实战项目，展示各种加密技术的实现和最佳实践。

## 🎯 项目简介

本项目提供了 Java 加密算法的全面学习案例，包括：

- **对称加密**: AES、DES、3DES
- **非对称加密**: RSA、椭圆曲线加密
- **消息摘要**: MD5、SHA系列、HMAC
- **数字签名**: RSA签名、DSA、ECDSA
- **密码哈希**: PBKDF2、BCrypt
- **现代加密**: AES-GCM、密钥管理

## 🛠️ 技术栈

- **JDK**: 17 (使用最新语言特性)
- **构建工具**: Maven 3.8+
- **加密库**: Bouncy Castle 1.78.1
- **测试框架**: JUnit 5.10.2
- **日志框架**: SLF4J + Logback
- **代码生成**: Lombok 1.18.32

## 📁 项目结构

```
src/main/java/com/javalaabs/crypto/
├── symmetric/                # 对称加密算法
│   ├── AESCrypto.java       # AES加密（CBC/GCM模式）
│   └── DESCrypto.java       # DES和3DES加密
├── asymmetric/              # 非对称加密算法
│   └── RSACrypto.java       # RSA加密和数字签名
├── digest/                  # 消息摘要算法
│   └── DigestCrypto.java    # MD5、SHA系列、HMAC
├── password/                # 密码哈希算法
│   └── PasswordHashCrypto.java # PBKDF2、BCrypt
├── encoding/                # 编码转换算法
│   ├── BaseConverter.java   # 进制转换（2-36进制、Base62）
│   ├── Base64Encoder.java   # Base64编码（标准、URL安全、MIME）
│   ├── UrlEncoder.java      # URL编码解码
│   └── TextEncoder.java     # 文本编码（十六进制、Unicode、ASCII等）
├── utils/                   # 工具类
│   ├── CryptoUtils.java     # 通用加密工具
│   └── CryptoException.java # 加密异常
└── CryptoDemo.java          # 综合演示程序

src/test/java/com/javalaabs/crypto/
├── symmetric/               # 对称加密测试
│   └── AESCryptoTest.java   # AES加密测试
└── [其他测试类...]
```