---
title: Spring 依赖注入详解
description: Spring 依赖注入详解
tags: [Spring Dependency]
category: Spring
date: 2025-09-10
---

# Spring 依赖注入详解

## 🎯 概述

依赖注入（Dependency Injection，DI）是 Spring 框架实现控制反转（IoC）的具体方式。它通过外部容器来管理对象之间的依赖关系，而不是由对象自己创建或查找依赖，从而实现了松耦合的设计。

## 🔍 什么是依赖注入

### 传统方式 vs 依赖注入

#### 传统方式（紧耦合）
```java
public class OrderService {
    private PaymentService paymentService;
    private EmailService emailService;

    public OrderService() {
        // 对象自己创建依赖，紧耦合
        this.paymentService = new PaymentService();
        this.emailService = new EmailService();
    }
}
```

#### 依赖注入方式（松耦合）
```java
public class OrderService {
    private final PaymentService paymentService;
    private final EmailService emailService;

    // 依赖通过构造函数注入
    public OrderService(PaymentService paymentService, EmailService emailService) {
        this.paymentService = paymentService;
        this.emailService = emailService;
    }
}
```

## 📋 依赖注入的类型

### 1. 构造函数注入（Constructor Injection）

#### 基本用法
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Spring 自动注入依赖
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }
}
```

#### 单一构造函数（Spring 4.3+）
```java
@Service
public class UserService {
    private final UserRepository userRepository;

    // 只有一个构造函数时，@Autowired 可以省略
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

#### 多构造函数场景
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    // 主构造函数
    @Autowired
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // 备用构造函数
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.emailService = new DefaultEmailService();
    }
}
```

### 2. Setter 方法注入（Setter Injection）

```java
@Service
public class UserService {
    private UserRepository userRepository;
    private EmailService emailService;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
```

### 3. 字段注入（Field Injection）

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;
}
```

## 🎨 注入方式对比

| 注入方式 | 优点 | 缺点 | 使用场景 |
|---------|------|------|----------|
| 构造函数注入 | • 保证依赖不可变<br>• 支持 final 字段<br>• 便于测试<br>• 明确依赖关系 | • 构造函数参数可能较多 | **推荐**，适用于必需依赖 |
| Setter 注入 | • 支持可选依赖<br>• 允许重新配置 | • 依赖可变<br>• 可能空指针异常 | 可选依赖，需要重新配置的场景 |
| 字段注入 | • 代码简洁 | • 难以测试<br>• 隐藏依赖关系<br>• 无法使用 final | **不推荐**，仅在测试代码中使用 |

## ⚙️ 高级注入技术

### 1. @Qualifier 限定符

```java
public interface MessageService {
    void sendMessage(String message);
}

@Service
@Qualifier("email")
public class EmailMessageService implements MessageService {
    @Override
    public void sendMessage(String message) {
        System.out.println("Email: " + message);
    }
}

@Service
@Qualifier("sms")
public class SmsMessageService implements MessageService {
    @Override
    public void sendMessage(String message) {
        System.out.println("SMS: " + message);
    }
}

@Service
public class NotificationService {
    private final MessageService emailService;
    private final MessageService smsService;

    public NotificationService(
            @Qualifier("email") MessageService emailService,
            @Qualifier("sms") MessageService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
}
```

### 2. @Primary 主要 Bean

```java
@Service
@Primary
public class DefaultPaymentService implements PaymentService {
    // 默认支付服务实现
}

@Service
public class AlipayPaymentService implements PaymentService {
    // 支付宝支付服务实现
}

@Service
public class OrderService {
    private final PaymentService paymentService;

    // 会注入 DefaultPaymentService（标记为 @Primary）
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

### 3. 集合注入

```java
@Service
public class PaymentProcessor {
    private final List<PaymentService> paymentServices;
    private final Map<String, PaymentService> paymentServiceMap;

    public PaymentProcessor(
            List<PaymentService> paymentServices,
            Map<String, PaymentService> paymentServiceMap) {
        this.paymentServices = paymentServices;
        this.paymentServiceMap = paymentServiceMap;
    }

    public void processPayment(String type, BigDecimal amount) {
        // 根据类型选择支付服务
        PaymentService service = paymentServiceMap.get(type + "PaymentService");
        if (service != null) {
            service.pay(amount);
        }
    }
}
```

### 4. Optional 依赖注入

```java
@Service
public class UserService {
    private final UserRepository userRepository;
    private final Optional<CacheService> cacheService;

    public UserService(
            UserRepository userRepository,
            Optional<CacheService> cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public User findUser(Long id) {
        // 使用缓存（如果可用）
        if (cacheService.isPresent()) {
            User cachedUser = cacheService.get().getUser(id);
            if (cachedUser != null) {
                return cachedUser;
            }
        }

        User user = userRepository.findById(id);

        // 缓存结果（如果缓存可用）
        cacheService.ifPresent(cache -> cache.putUser(id, user));

        return user;
    }
}
```

## 🔄 循环依赖处理

### 问题示例
```java
@Service
public class ServiceA {
    private final ServiceB serviceB;

    public ServiceA(ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
// 这会导致循环依赖异常
```

### 解决方案

#### 1. 使用 @Lazy 延迟初始化
```java
@Service
public class ServiceA {
    private final ServiceB serviceB;

    public ServiceA(@Lazy ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
```

#### 2. 使用 Setter 注入
```java
@Service
public class ServiceA {
    private ServiceB serviceB;

    @Autowired
    public void setServiceB(ServiceB serviceB) {
        this.serviceB = serviceB;
    }
}

@Service
public class ServiceB {
    private final ServiceA serviceA;

    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
}
```

#### 3. 重构设计（推荐）
```java
// 提取公共接口或服务
@Service
public class CommonService {
    // 公共逻辑
}

@Service
public class ServiceA {
    private final CommonService commonService;

    public ServiceA(CommonService commonService) {
        this.commonService = commonService;
    }
}

@Service
public class ServiceB {
    private final CommonService commonService;

    public ServiceB(CommonService commonService) {
        this.commonService = commonService;
    }
}
```

## 📊 依赖注入的生命周期

### 1. Bean 创建和注入流程

```
1. 读取 Bean 定义
    ↓
2. 实例化 Bean
    ↓
3. 设置属性（依赖注入）
    ↓
4. 调用 Aware 接口方法
    ↓
5. BeanPostProcessor 前置处理
    ↓
6. 初始化方法
    ↓
7. BeanPostProcessor 后置处理
    ↓
8. Bean 可用
```

### 2. 自定义初始化

```java
@Service
public class UserService implements InitializingBean, DisposableBean {
    private UserRepository userRepository;
    private boolean initialized = false;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // 所有依赖注入完成后调用
        System.out.println("UserService 初始化完成");
        this.initialized = true;
    }

    @PostConstruct
    public void init() {
        // 另一种初始化方式
        System.out.println("@PostConstruct 初始化");
    }

    @Override
    public void destroy() throws Exception {
        // Bean 销毁时调用
        System.out.println("UserService 销毁");
    }

    @PreDestroy
    public void cleanup() {
        // 另一种销毁方式
        System.out.println("@PreDestroy 清理");
    }
}
```

## 🧪 测试中的依赖注入

### 1. 单元测试
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    @Test
    void testCreateUser() {
        // 测试逻辑
        when(userRepository.save(any(User.class)))
                .thenReturn(new User("张三"));

        User user = userService.createUser("张三");

        assertNotNull(user);
        assertEquals("张三", user.getName());
    }
}
```

### 2. 集成测试
```java
@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @Test
    void testCreateUser() {
        // 集成测试逻辑
        User user = userService.createUser("李四");

        assertNotNull(user);
        verify(emailService).sendWelcomeEmail(user.getEmail());
    }
}
```

## 🔧 配置依赖注入

### 1. Java 配置
```java
@Configuration
public class ServiceConfig {

    @Bean
    public UserRepository userRepository() {
        return new JpaUserRepository();
    }

    @Bean
    public EmailService emailService() {
        return new SmtpEmailService();
    }

    @Bean
    public UserService userService(
            UserRepository userRepository,
            EmailService emailService) {
        return new UserService(userRepository, emailService);
    }
}
```

### 2. XML 配置
```xml
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <bean id="userRepository" class="com.example.JpaUserRepository"/>

    <bean id="emailService" class="com.example.SmtpEmailService"/>

    <bean id="userService" class="com.example.UserService">
        <constructor-arg ref="userRepository"/>
        <constructor-arg ref="emailService"/>
    </bean>

</beans>
```

## 🎯 最佳实践

### 1. 优先使用构造函数注入
```java
// ✅ 推荐
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ 避免
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 2. 使用接口而非实现类
```java
// ✅ 推荐
@Service
public class UserService {
    private final UserRepository userRepository; // 接口

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ 避免
@Service
public class UserService {
    private final JpaUserRepository userRepository; // 具体实现

    public UserService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 3. 明确依赖关系
```java
// ✅ 推荐：明确标识必需和可选依赖
@Service
public class UserService {
    private final UserRepository userRepository; // 必需
    private final Optional<CacheService> cacheService; // 可选

    public UserService(
            UserRepository userRepository,
            Optional<CacheService> cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }
}
```

### 4. 避免过度依赖
```java
// ❌ 避免：依赖过多，违反单一职责原则
@Service
public class UserService {
    public UserService(
            UserRepository userRepository,
            EmailService emailService,
            SmsService smsService,
            PaymentService paymentService,
            LoggingService loggingService,
            CacheService cacheService,
            SecurityService securityService) {
        // 太多依赖，考虑重构
    }
}

// ✅ 推荐：拆分职责
@Service
public class UserService {
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public UserService(
            UserRepository userRepository,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
}
```

## 📝 小结

Spring 依赖注入提供了强大而灵活的依赖管理机制：

- **多种注入方式** - 构造函数、Setter、字段注入
- **高级功能** - @Qualifier、@Primary、集合注入
- **循环依赖处理** - @Lazy、重构设计
- **生命周期集成** - 与 Bean 生命周期紧密结合
- **测试友好** - 便于单元测试和集成测试

掌握依赖注入是构建松耦合、可测试、可维护应用程序的关键技能。推荐优先使用构造函数注入，并遵循最佳实践来设计清晰的依赖关系。