---
title: Spring IoC 容器详解
description: Spring IoC 容器详解
tags: [Spring IoC]
category: Spring
date: 2025-09-10
---

# Spring IoC 容器详解

## 🎯 概述

Spring IoC（Inversion of Control，控制反转）容器是 Spring 框架的核心，它负责管理应用程序中的对象（称为 Bean）的生命周期和依赖关系。通过 IoC 容器，我们可以实现松耦合的应用程序架构。

## 📚 什么是控制反转（IoC）

### 传统编程方式
```java
public class OrderService {
    private PaymentService paymentService;

    public OrderService() {
        // 对象直接创建依赖，紧耦合
        this.paymentService = new PaymentService();
    }
}
```

### IoC 方式
```java
public class OrderService {
    private PaymentService paymentService;

    // 依赖通过构造函数注入，松耦合
    public OrderService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
```

## 🏗️ Spring IoC 容器架构

### 核心接口

#### BeanFactory
```java
public interface BeanFactory {
    Object getBean(String name) throws BeansException;
    <T> T getBean(Class<T> requiredType) throws BeansException;
    boolean containsBean(String name);
    boolean isSingleton(String name) throws NoSuchBeanDefinitionException;
    // ... 其他方法
}
```

#### ApplicationContext
```java
public interface ApplicationContext extends BeanFactory,
    MessageSource, ApplicationEventPublisher, ResourcePatternResolver {

    String getApplicationName();
    ApplicationContext getParent();
    AutowireCapableBeanFactory getAutowireCapableBeanFactory();
    // ... 其他方法
}
```

### 容器层次结构

```
ApplicationContext (高级容器)
    ├── 国际化支持 (MessageSource)
    ├── 事件发布 (ApplicationEventPublisher)
    ├── 资源访问 (ResourcePatternResolver)
    └── BeanFactory (基础容器)
        ├── Bean 实例化
        ├── 依赖注入
        └── 生命周期管理
```

## ⚙️ 容器配置方式

### 1. XML 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- 定义 Bean -->
    <bean id="userService" class="com.example.UserService">
        <property name="userRepository" ref="userRepository"/>
    </bean>

    <bean id="userRepository" class="com.example.UserRepository"/>

</beans>
```

### 2. 注解配置

```java
@Configuration
@ComponentScan(basePackages = "com.example")
public class AppConfig {

    @Bean
    public UserService userService(UserRepository userRepository) {
        return new UserService(userRepository);
    }

    @Bean
    public UserRepository userRepository() {
        return new UserRepository();
    }
}
```

### 3. 组件扫描配置

```java
@Component
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // 业务方法
}

@Repository
public class UserRepository {
    // 数据访问方法
}
```

## 🚀 容器启动过程

### ApplicationContext 启动流程

```java
public class SpringApplicationDemo {
    public static void main(String[] args) {
        // 1. 创建容器
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // 2. 获取 Bean
        UserService userService = context.getBean(UserService.class);

        // 3. 使用 Bean
        userService.createUser("张三");

        // 4. 关闭容器（如果是 ConfigurableApplicationContext）
        ((ConfigurableApplicationContext) context).close();
    }
}
```

### 容器内部启动步骤

1. **加载配置** - 读取配置元数据
2. **解析配置** - 解析 Bean 定义
3. **注册 Bean 定义** - 将 Bean 定义注册到容器
4. **实例化单例 Bean** - 创建单例作用域的 Bean
5. **依赖注入** - 注入依赖关系
6. **初始化 Bean** - 调用初始化方法
7. **容器就绪** - 容器启动完成

## 🔧 容器高级特性

### 1. Bean 定义继承

```xml
<bean id="abstractBean" abstract="true">
    <property name="timeout" value="5000"/>
    <property name="retryCount" value="3"/>
</bean>

<bean id="userService" class="com.example.UserService" parent="abstractBean">
    <property name="maxUsers" value="1000"/>
</bean>
```

### 2. 容器扩展点

#### BeanPostProcessor
```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("初始化前处理: " + beanName);
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("初始化后处理: " + beanName);
        return bean;
    }
}
```

#### BeanFactoryPostProcessor
```java
@Component
public class CustomBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        System.out.println("Bean 工厂后处理器执行");
        // 可以修改 Bean 定义
    }
}
```

### 3. 环境抽象

```java
@Configuration
@PropertySource("classpath:application.properties")
public class EnvironmentConfig {

    @Autowired
    private Environment environment;

    @Bean
    public DataSource dataSource() {
        String url = environment.getProperty("database.url");
        String username = environment.getProperty("database.username");
        // 创建数据源
        return new DataSource(url, username);
    }
}
```

## 📊 容器性能优化

### 1. 延迟初始化

```java
@Component
@Lazy
public class HeavyService {
    // 只有在首次使用时才会被创建
}
```

### 2. 条件化 Bean

```java
@Component
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public class FeatureService {
    // 只有在配置启用时才会创建
}
```

### 3. Profile 环境

```java
@Configuration
@Profile("production")
public class ProductionConfig {

    @Bean
    public DataSource dataSource() {
        // 生产环境数据源
        return new ProductionDataSource();
    }
}

@Configuration
@Profile("development")
public class DevelopmentConfig {

    @Bean
    public DataSource dataSource() {
        // 开发环境数据源
        return new H2DataSource();
    }
}
```

## 🎯 最佳实践

### 1. 构造函数注入优于字段注入

```java
// ✅ 推荐：构造函数注入
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}

// ❌ 不推荐：字段注入
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
}
```

### 2. 使用接口编程

```java
@Service
public class UserService {
    private final UserRepository userRepository; // 接口类型

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 3. 合理使用 Bean 作用域

```java
@Service
@Scope("singleton") // 默认，单例
public class UserService {
}

@Controller
@Scope("prototype") // 原型，每次请求新实例
public class UserController {
}
```

## 🔍 调试和监控

### 1. 容器事件监听

```java
@Component
public class ContainerEventListener {

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        System.out.println("容器刷新完成");
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        System.out.println("容器关闭");
    }
}
```

### 2. Bean 信息查看

```java
@Component
public class ContainerInfoService {

    @Autowired
    private ApplicationContext applicationContext;

    public void printBeanInfo() {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        System.out.println("容器中的 Bean 数量: " + beanNames.length);

        for (String beanName : beanNames) {
            System.out.println("Bean 名称: " + beanName);
        }
    }
}
```

## 📝 小结

Spring IoC 容器是一个强大的依赖注入容器，它提供了：

- **松耦合架构** - 通过依赖注入实现组件解耦
- **灵活配置** - 支持多种配置方式
- **生命周期管理** - 完整的 Bean 生命周期控制
- **扩展机制** - 丰富的扩展点
- **环境支持** - 多环境配置支持

掌握 IoC 容器是学习 Spring 框架的基础，它为构建企业级应用提供了坚实的基础架构。