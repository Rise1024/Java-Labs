---
title: Spring 配置详解
description: Spring 配置详解
tags: [Spring Conf]
category: Spring/核心技术
date: 2025-09-10
---

# Spring 配置详解

## 🎯 概述

Spring 框架提供了多种配置方式来定义和管理应用程序的组件。随着 Spring 的发展，配置方式从最初的 XML 配置逐步演进到注解配置和 Java 配置，每种方式都有其适用场景和优势。

## 📚 配置方式演进历程

### 发展历程
```
Spring 1.x - 2.x
├── XML 配置为主
└── 手动配置所有 Bean

Spring 2.5+
├── 引入注解配置
├── @Component、@Service、@Repository
└── 组件扫描 @ComponentScan

Spring 3.0+
├── Java 配置 @Configuration
├── @Bean 方法定义
└── 类型安全的配置

Spring 4.0+
├── 条件化配置 @Conditional
├── Profile 环境配置
└── @Enable* 注解

Spring 5.0+
├── 响应式配置
├── 函数式 Bean 注册
└── AOT 编译支持
```

## 🗂️ XML 配置方式

### 1. 基础 XML 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- Bean 定义 -->
    <bean id="userRepository" class="com.example.repository.UserRepositoryImpl"/>

    <!-- 构造函数注入 -->
    <bean id="userService" class="com.example.service.UserServiceImpl">
        <constructor-arg ref="userRepository"/>
    </bean>

    <!-- 属性注入 -->
    <bean id="emailService" class="com.example.service.EmailServiceImpl">
        <property name="smtpHost" value="smtp.example.com"/>
        <property name="smtpPort" value="587"/>
        <property name="userRepository" ref="userRepository"/>
    </bean>

</beans>
```

### 2. 高级 XML 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:util="http://www.springframework.org/schema/util"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/util
           http://www.springframework.org/schema/util/spring-util.xsd">

    <!-- 属性文件配置 -->
    <context:property-placeholder location="classpath:application.properties"/>

    <!-- 集合配置 -->
    <bean id="configService" class="com.example.service.ConfigService">
        <property name="supportedFormats">
            <list>
                <value>JSON</value>
                <value>XML</value>
                <value>YAML</value>
            </list>
        </property>
        <property name="defaultConfig">
            <map>
                <entry key="timeout" value="30"/>
                <entry key="retries" value="3"/>
            </map>
        </property>
    </bean>

    <!-- Bean 作用域 -->
    <bean id="prototypeService"
          class="com.example.service.PrototypeService"
          scope="prototype"/>

    <!-- 延迟初始化 -->
    <bean id="heavyService"
          class="com.example.service.HeavyService"
          lazy-init="true"/>

    <!-- 初始化和销毁方法 -->
    <bean id="resourceService"
          class="com.example.service.ResourceService"
          init-method="init"
          destroy-method="cleanup"/>

</beans>
```

### 3. 命名空间配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:aop="http://www.springframework.org/schema/aop"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="
           http://www.springframework.org/schema/beans
           http://www.springframework.org/schema/beans/spring-beans.xsd
           http://www.springframework.org/schema/context
           http://www.springframework.org/schema/context/spring-context.xsd
           http://www.springframework.org/schema/aop
           http://www.springframework.org/schema/aop/spring-aop.xsd
           http://www.springframework.org/schema/tx
           http://www.springframework.org/schema/tx/spring-tx.xsd">

    <!-- 组件扫描 -->
    <context:component-scan base-package="com.example"/>

    <!-- 启用注解配置 -->
    <context:annotation-config/>

    <!-- AOP 配置 -->
    <aop:aspectj-autoproxy/>

    <!-- 事务配置 -->
    <tx:annotation-driven transaction-manager="transactionManager"/>

</beans>
```

## 🏷️ 注解配置方式

### 1. 基础注解

```java
// 组件注解
@Component
public class UserValidator {
    public boolean validate(User user) {
        return user != null && user.getName() != null;
    }
}

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserValidator userValidator;

    public User createUser(User user) {
        if (!userValidator.validate(user)) {
            throw new IllegalArgumentException("用户信息无效");
        }
        return userRepository.save(user);
    }
}

@Repository
public class UserRepositoryImpl implements UserRepository {
    // 数据访问逻辑
}

@Controller
public class UserController {
    @Autowired
    private UserService userService;

    // 控制器逻辑
}
```

### 2. 依赖注入注解

```java
@Service
public class OrderService {

    // 字段注入
    @Autowired
    private PaymentService paymentService;

    // 构造函数注入（推荐）
    private final UserService userService;
    private final EmailService emailService;

    public OrderService(UserService userService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
    }

    // Setter 注入
    private NotificationService notificationService;

    @Autowired
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // 限定符注入
    @Autowired
    @Qualifier("primaryPaymentService")
    private PaymentService primaryPaymentService;

    // 可选注入
    @Autowired(required = false)
    private Optional<CacheService> cacheService;

    // 按名称注入
    @Resource(name = "emailPaymentService")
    private PaymentService emailPaymentService;
}
```

### 3. 生命周期注解

```java
@Service
public class DatabaseService {

    @PostConstruct
    public void init() {
        System.out.println("数据库连接初始化");
        // 初始化数据库连接
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("清理数据库连接");
        // 清理资源
    }
}
```

### 4. 配置注解

```java
@Component
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version:1.0}")
    private String appVersion;

    @Value("#{systemProperties['java.home']}")
    private String javaHome;

    // 获取环境变量
    @Value("${DATABASE_URL:jdbc:h2:mem:testdb}")
    private String databaseUrl;

    // 复杂表达式
    @Value("#{T(java.lang.Math).random() * 100}")
    private double randomNumber;
}
```

## ☕ Java 配置方式

### 1. 基础 Java 配置

```java
@Configuration
public class AppConfig {

    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }

    @Bean
    public UserService userService() {
        return new UserServiceImpl(userRepository());
    }

    @Bean
    public EmailService emailService() {
        EmailServiceImpl emailService = new EmailServiceImpl();
        emailService.setSmtpHost("smtp.example.com");
        emailService.setSmtpPort(587);
        return emailService;
    }
}
```

### 2. 高级 Java 配置

```java
@Configuration
@EnableTransactionManagement
@EnableScheduling
@ComponentScan(basePackages = "com.example")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Autowired
    private Environment environment;

    @Bean
    @Primary
    public DataSource primaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getProperty("primary.datasource.url"));
        config.setUsername(environment.getProperty("primary.datasource.username"));
        config.setPassword(environment.getProperty("primary.datasource.password"));
        config.setMaximumPoolSize(20);
        return new HikariDataSource(config);
    }

    @Bean
    @Qualifier("secondary")
    public DataSource secondaryDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(environment.getProperty("secondary.datasource.url"));
        config.setUsername(environment.getProperty("secondary.datasource.username"));
        config.setPassword(environment.getProperty("secondary.datasource.password"));
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }

    @Bean
    public TransactionManager transactionManager() {
        return new DataSourceTransactionManager(primaryDataSource());
    }
}
```

### 3. 条件化配置

```java
@Configuration
public class ConditionalConfig {

    @Bean
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public CacheService cacheService() {
        return new RedisCacheService();
    }

    @Bean
    @ConditionalOnMissingBean(CacheService.class)
    public CacheService defaultCacheService() {
        return new NoOpCacheService();
    }

    @Bean
    @ConditionalOnClass(name = "redis.clients.jedis.Jedis")
    public RedisTemplate redisTemplate() {
        return new RedisTemplate();
    }

    @Bean
    @Profile("development")
    public DataSource devDataSource() {
        return new H2DataSource();
    }

    @Bean
    @Profile("production")
    public DataSource prodDataSource() {
        return new PostgresDataSource();
    }
}
```

### 4. 配置类组合

```java
@Configuration
@Import({DatabaseConfig.class, CacheConfig.class, SecurityConfig.class})
public class MainConfig {
}

@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        // 数据源配置
    }
}

@Configuration
public class CacheConfig {
    @Bean
    public CacheManager cacheManager() {
        // 缓存配置
    }
}

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // 安全配置
}
```

## 🌍 环境和 Profile 配置

### 1. Profile 配置

```java
@Configuration
@Profile("development")
public class DevelopmentConfig {

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }

    @Bean
    public MailSender mailSender() {
        return new MockMailSender(); // 开发环境使用模拟邮件发送
    }
}

@Configuration
@Profile("production")
public class ProductionConfig {

    @Bean
    public DataSource dataSource() {
        // 生产环境数据库配置
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://prod-db:5432/myapp");
        return new HikariDataSource(config);
    }

    @Bean
    public MailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.company.com");
        return mailSender;
    }
}

// 激活 Profile
@ActiveProfiles("development")
@SpringBootTest
public class ApplicationTest {
    // 测试代码
}
```

### 2. 环境抽象

```java
@Service
public class ConfigService {

    @Autowired
    private Environment environment;

    public void printConfig() {
        // 获取激活的 Profile
        String[] activeProfiles = environment.getActiveProfiles();
        System.out.println("激活的 Profile: " + Arrays.toString(activeProfiles));

        // 获取属性值
        String appName = environment.getProperty("app.name", "DefaultApp");
        Integer port = environment.getProperty("server.port", Integer.class, 8080);

        // 检查 Profile 是否激活
        if (environment.acceptsProfiles(Profiles.of("development"))) {
            System.out.println("当前是开发环境");
        }

        // 获取系统属性
        String javaVersion = environment.getProperty("java.version");
        String userHome = environment.getProperty("user.home");
    }
}
```

### 3. 属性源配置

```java
@Configuration
@PropertySource("classpath:application.properties")
@PropertySource("classpath:database.properties")
@PropertySource(value = "classpath:optional.properties", ignoreResourceNotFound = true)
public class PropertyConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }
}

// 属性文件内容示例
// application.properties
app.name=MyApplication
app.version=1.0.0
server.port=8080

// database.properties
db.url=jdbc:mysql://localhost:3306/myapp
db.username=user
db.password=password
db.pool.max-size=20
```

## 🔧 自定义配置

### 1. 自定义属性类

```java
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {

    private String name;
    private String version;
    private Security security = new Security();
    private List<String> supportedLanguages = new ArrayList<>();
    private Map<String, String> metadata = new HashMap<>();

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public List<String> getSupportedLanguages() { return supportedLanguages; }
    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }

    public Map<String, String> getMetadata() { return metadata; }
    public void setMetadata(Map<String, String> metadata) { this.metadata = metadata; }

    public static class Security {
        private boolean enabled = true;
        private String tokenExpiration = "24h";

        // Getters and Setters
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        public String getTokenExpiration() { return tokenExpiration; }
        public void setTokenExpiration(String tokenExpiration) {
            this.tokenExpiration = tokenExpiration;
        }
    }
}

// application.properties
app.name=MyApp
app.version=2.0.0
app.security.enabled=true
app.security.token-expiration=12h
app.supported-languages[0]=zh
app.supported-languages[1]=en
app.metadata.author=张三
app.metadata.email=zhangsan@example.com
```

### 2. 自定义配置处理器

```java
@Component
public class CustomConfigurationProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = bean.getClass();

        // 处理自定义注解
        if (clazz.isAnnotationPresent(CustomConfig.class)) {
            CustomConfig config = clazz.getAnnotation(CustomConfig.class);
            System.out.println("处理自定义配置: " + config.value());

            // 执行自定义配置逻辑
            processCustomConfiguration(bean, config);
        }

        return bean;
    }

    private void processCustomConfiguration(Object bean, CustomConfig config) {
        // 自定义配置处理逻辑
    }
}

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomConfig {
    String value();
}

@Service
@CustomConfig("userService")
public class UserService {
    // 服务实现
}
```

### 3. 外部化配置

```java
@Configuration
@EnableConfigurationProperties({AppProperties.class, DatabaseProperties.class})
public class ExternalConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }
}

@ConfigurationProperties(prefix = "app.database")
@Component
public class DatabaseProperties {
    private String url;
    private String username;
    private String password;
    private Pool pool = new Pool();

    public static class Pool {
        private int maxSize = 10;
        private int minSize = 2;
        private long timeout = 30000;

        // Getters and Setters
    }

    // Getters and Setters
}

// application.yml
app:
  database:
    url: jdbc:postgresql://localhost:5432/myapp
    username: user
    password: secret
    pool:
      max-size: 20
      min-size: 5
      timeout: 60000
  datasource:
    url: ${app.database.url}
    username: ${app.database.username}
    password: ${app.database.password}
    maximum-pool-size: ${app.database.pool.max-size}
```

## 🔄 配置方式对比

| 特性 | XML 配置 | 注解配置 | Java 配置 |
|------|----------|----------|-----------|
| **类型安全** | ❌ | ⚠️ | ✅ |
| **重构友好** | ❌ | ⚠️ | ✅ |
| **集中管理** | ✅ | ❌ | ✅ |
| **学习曲线** | 陡峭 | 平缓 | 中等 |
| **灵活性** | 高 | 中等 | 最高 |
| **可测试性** | 低 | 中等 | 高 |
| **IDE 支持** | 好 | 很好 | 最好 |

### 推荐使用策略

```java
// 现代 Spring 应用推荐配置策略

// 1. 基础配置使用 Java 配置
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// 2. 业务组件使用注解
@Service
@Transactional
public class UserService {
    // 业务逻辑
}

// 3. 复杂配置使用 Java 配置类
@Configuration
public class DatabaseConfig {
    @Bean
    @Primary
    public DataSource dataSource() {
        // 复杂的数据源配置
    }
}

// 4. 属性外部化使用 @ConfigurationProperties
@ConfigurationProperties(prefix = "app")
@Component
public class AppProperties {
    // 属性绑定
}
```

## 🎯 最佳实践

### 1. 配置组织

```java
// ✅ 推荐：按功能模块组织配置
@Configuration
public class WebConfig {
    // Web 相关配置
}

@Configuration
public class DataConfig {
    // 数据相关配置
}

@Configuration
public class SecurityConfig {
    // 安全相关配置
}

// 主配置类导入模块配置
@Configuration
@Import({WebConfig.class, DataConfig.class, SecurityConfig.class})
public class AppConfig {
}
```

### 2. 条件化配置

```java
// ✅ 推荐：使用条件注解
@Configuration
public class CacheConfig {

    @Bean
    @ConditionalOnProperty(name = "cache.type", havingValue = "redis")
    public CacheManager redisCacheManager() {
        return new RedisCacheManager(redisTemplate());
    }

    @Bean
    @ConditionalOnProperty(name = "cache.type", havingValue = "caffeine")
    public CacheManager caffeineCacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
```

### 3. 配置验证

```java
@ConfigurationProperties(prefix = "app")
@Validated
@Component
public class AppProperties {

    @NotEmpty
    private String name;

    @Min(1)
    @Max(65535)
    private int port;

    @Email
    private String adminEmail;

    @Valid
    private Database database = new Database();

    public static class Database {
        @NotEmpty
        private String url;

        @Range(min = 1, max = 100)
        private int maxConnections;

        // Getters and Setters
    }

    // Getters and Setters
}
```

### 4. 敏感信息处理

```java
@Configuration
public class SecurityConfig {

    // ✅ 推荐：使用环境变量
    @Value("${DATABASE_PASSWORD}")
    private String databasePassword;

    // ✅ 推荐：使用 Spring Cloud Config 或外部配置
    @Value("${app.security.secret}")
    private String secret;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ❌ 避免：硬编码敏感信息
    // private static final String SECRET = "hardcoded-secret";
}
```

## 📝 小结

Spring 配置管理提供了丰富而灵活的方式：

- **XML 配置** - 传统方式，适合复杂的企业级配置
- **注解配置** - 简洁直观，适合组件定义
- **Java 配置** - 类型安全，适合复杂逻辑配置
- **属性外部化** - 环境相关配置的最佳实践
- **Profile 支持** - 多环境配置管理
- **条件化配置** - 智能的配置选择

现代 Spring 应用推荐使用 Java 配置 + 注解配置的组合方式，配合外部化属性配置，既保证了类型安全，又提供了足够的灵活性。在配置设计时要注意模块化、条件化和安全性，为不同环境提供合适的配置策略。