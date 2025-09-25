---
title: Spring Bean 生命周期详解
description: Spring Bean 生命周期详解
tags: [Spring Bean]
category: Spring
date: 2025-09-10
---

# Spring Bean 生命周期详解

## 🎯 概述

Spring Bean 的生命周期是指从 Bean 的实例化到销毁的整个过程。Spring 容器负责管理 Bean 的完整生命周期，提供了多个扩展点让开发者可以在不同阶段执行自定义逻辑。

## 🔄 Bean 生命周期完整流程

### 生命周期阶段图

```
Bean 定义加载
    ↓
1. 实例化（Instantiation）
    ↓
2. 属性赋值（Populate Properties）
    ↓
3. Aware 接口回调
    ↓
4. BeanPostProcessor 前置处理
    ↓
5. 初始化（Initialization）
    ↓
6. BeanPostProcessor 后置处理
    ↓
7. Bean 就绪使用
    ↓
8. 销毁（Destruction）
```

### 详细流程说明

1. **Bean 定义加载** - Spring 读取配置元数据
2. **实例化** - 调用构造函数创建 Bean 实例
3. **属性赋值** - 依赖注入，设置 Bean 属性
4. **Aware 接口回调** - 回调各种 Aware 接口方法
5. **前置处理** - BeanPostProcessor.postProcessBeforeInitialization()
6. **初始化** - 执行初始化方法
7. **后置处理** - BeanPostProcessor.postProcessAfterInitialization()
8. **使用阶段** - Bean 可以被应用程序使用
9. **销毁** - 容器关闭时执行销毁方法

## 🏗️ Bean 实例化阶段

### 1. 构造函数调用

```java
@Component
public class UserService {
    private final UserRepository userRepository;

    // 1. 构造函数被调用，创建 Bean 实例
    public UserService(UserRepository userRepository) {
        System.out.println("UserService 构造函数执行");
        this.userRepository = userRepository;
    }
}
```

### 2. 实例化后置处理器

```java
@Component
public class CustomInstantiationPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) {
        System.out.println("实例化前处理: " + beanName);
        return null; // 返回 null 继续正常实例化
    }

    @Override
    public boolean postProcessAfterInstantiation(Object bean, String beanName) {
        System.out.println("实例化后处理: " + beanName);
        return true; // 返回 true 继续属性注入
    }
}
```

## 🔧 属性赋值阶段

### 1. 依赖注入

```java
@Service
public class OrderService {
    @Autowired
    private PaymentService paymentService; // 2. 属性注入

    @Autowired
    private EmailService emailService;

    public OrderService() {
        System.out.println("OrderService 构造函数执行");
        // 此时 paymentService 和 emailService 还是 null
    }

    @PostConstruct
    public void init() {
        System.out.println("初始化时 paymentService 不为空: " + (paymentService != null));
    }
}
```

### 2. 属性值处理

```java
@Component
public class PropertyPostProcessor implements InstantiationAwareBeanPostProcessor {

    @Override
    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) {
        System.out.println("处理属性值: " + beanName);
        return pvs;
    }
}
```

## 🎭 Aware 接口回调

### 常用 Aware 接口

```java
@Component
public class AwareBean implements
        BeanNameAware,
        BeanFactoryAware,
        ApplicationContextAware,
        EnvironmentAware {

    private String beanName;
    private BeanFactory beanFactory;
    private ApplicationContext applicationContext;
    private Environment environment;

    @Override
    public void setBeanName(String name) {
        // 3. 设置 Bean 名称
        this.beanName = name;
        System.out.println("BeanNameAware: " + name);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        // 4. 设置 BeanFactory
        this.beanFactory = beanFactory;
        System.out.println("BeanFactoryAware 执行");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 5. 设置 ApplicationContext
        this.applicationContext = applicationContext;
        System.out.println("ApplicationContextAware 执行");
    }

    @Override
    public void setEnvironment(Environment environment) {
        // 6. 设置环境变量
        this.environment = environment;
        System.out.println("EnvironmentAware 执行");
    }
}
```

### Aware 接口执行顺序

1. **BeanNameAware.setBeanName()**
2. **BeanClassLoaderAware.setBeanClassLoader()**
3. **BeanFactoryAware.setBeanFactory()**
4. **EnvironmentAware.setEnvironment()**
5. **ApplicationContextAware.setApplicationContext()**

## 🔄 BeanPostProcessor 处理

### 1. 自定义 BeanPostProcessor

```java
@Component
public class CustomBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 7. 初始化前处理
        System.out.println("前置处理: " + beanName);

        // 可以返回代理对象
        if (bean instanceof UserService) {
            System.out.println("为 UserService 添加特殊处理");
        }

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 9. 初始化后处理
        System.out.println("后置处理: " + beanName);

        // 这里通常创建代理对象（如 AOP 代理）
        return bean;
    }
}
```

### 2. 内置 BeanPostProcessor

Spring 提供了多个内置的 BeanPostProcessor：

- **AutowiredAnnotationBeanPostProcessor** - 处理 @Autowired 注解
- **CommonAnnotationBeanPostProcessor** - 处理 @PostConstruct、@PreDestroy
- **ApplicationContextAwareProcessor** - 处理 Aware 接口
- **AnnotationAwareAspectJAutoProxyCreator** - 创建 AOP 代理

## 🚀 Bean 初始化阶段

### 1. 初始化方法的三种方式

#### @PostConstruct 注解
```java
@Service
public class UserService {

    @PostConstruct
    public void init() {
        // 8a. @PostConstruct 初始化
        System.out.println("@PostConstruct 初始化执行");
        // 执行初始化逻辑
    }
}
```

#### InitializingBean 接口
```java
@Service
public class UserService implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        // 8b. InitializingBean 初始化
        System.out.println("afterPropertiesSet 执行");
        // 执行初始化逻辑
    }
}
```

#### 自定义初始化方法
```java
@Service
public class UserService {

    public void customInit() {
        // 8c. 自定义初始化方法
        System.out.println("自定义初始化方法执行");
    }
}

@Configuration
public class Config {
    @Bean(initMethod = "customInit")
    public UserService userService() {
        return new UserService();
    }
}
```

### 2. 初始化方法执行顺序

1. **@PostConstruct** 标注的方法
2. **InitializingBean.afterPropertiesSet()**
3. **自定义初始化方法**

```java
@Service
public class InitOrderDemo implements InitializingBean {

    @PostConstruct
    public void postConstruct() {
        System.out.println("1. @PostConstruct 执行");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("2. afterPropertiesSet 执行");
    }

    public void customInit() {
        System.out.println("3. 自定义初始化方法执行");
    }
}
```

## 💀 Bean 销毁阶段

### 1. 销毁方法的三种方式

#### @PreDestroy 注解
```java
@Service
public class UserService {

    @PreDestroy
    public void cleanup() {
        System.out.println("@PreDestroy 清理资源");
        // 清理资源
    }
}
```

#### DisposableBean 接口
```java
@Service
public class UserService implements DisposableBean {

    @Override
    public void destroy() throws Exception {
        System.out.println("destroy 方法执行");
        // 清理资源
    }
}
```

#### 自定义销毁方法
```java
@Service
public class UserService {

    public void customDestroy() {
        System.out.println("自定义销毁方法执行");
    }
}

@Configuration
public class Config {
    @Bean(destroyMethod = "customDestroy")
    public UserService userService() {
        return new UserService();
    }
}
```

### 2. 销毁方法执行顺序

1. **@PreDestroy** 标注的方法
2. **DisposableBean.destroy()**
3. **自定义销毁方法**

## 🎯 完整生命周期示例

```java
@Component
public class LifecycleBean implements
        BeanNameAware,
        ApplicationContextAware,
        InitializingBean,
        DisposableBean {

    private String beanName;
    private ApplicationContext applicationContext;

    // 1. 构造函数
    public LifecycleBean() {
        System.out.println("1. 构造函数执行");
    }

    // 2. 依赖注入
    @Autowired
    private void setDependency(SomeDependency dependency) {
        System.out.println("2. 依赖注入完成");
    }

    // 3. BeanNameAware
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
        System.out.println("3. BeanNameAware.setBeanName: " + name);
    }

    // 4. ApplicationContextAware
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        System.out.println("4. ApplicationContextAware.setApplicationContext");
    }

    // 5. BeanPostProcessor 前置处理（在其他类中实现）

    // 6. @PostConstruct
    @PostConstruct
    public void postConstruct() {
        System.out.println("6. @PostConstruct 执行");
    }

    // 7. InitializingBean
    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("7. InitializingBean.afterPropertiesSet");
    }

    // 8. 自定义初始化方法
    public void customInit() {
        System.out.println("8. 自定义初始化方法");
    }

    // 9. BeanPostProcessor 后置处理（在其他类中实现）

    // Bean 可以使用了...

    // 10. @PreDestroy
    @PreDestroy
    public void preDestroy() {
        System.out.println("10. @PreDestroy 执行");
    }

    // 11. DisposableBean
    @Override
    public void destroy() throws Exception {
        System.out.println("11. DisposableBean.destroy");
    }

    // 12. 自定义销毁方法
    public void customDestroy() {
        System.out.println("12. 自定义销毁方法");
    }
}

@Configuration
public class LifecycleConfig {
    @Bean(initMethod = "customInit", destroyMethod = "customDestroy")
    public LifecycleBean lifecycleBean() {
        return new LifecycleBean();
    }
}
```

## 🔍 生命周期监控和调试

### 1. 生命周期事件监听

```java
@Component
public class BeanLifecycleListener {

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        System.out.println("容器刷新完成，所有 Bean 初始化完毕");
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        System.out.println("容器关闭，开始销毁 Bean");
    }
}
```

### 2. 自定义生命周期监控

```java
@Component
public class BeanLifecycleMonitor implements BeanPostProcessor {

    private final Map<String, Long> beanCreationTimes = new ConcurrentHashMap<>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        beanCreationTimes.put(beanName, System.currentTimeMillis());
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        long startTime = beanCreationTimes.get(beanName);
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Bean " + beanName + " 初始化耗时: " + duration + "ms");
        return bean;
    }
}
```

## ⚙️ Bean 作用域对生命周期的影响

### 1. Singleton 作用域（默认）

```java
@Service
@Scope("singleton") // 默认
public class SingletonService {
    // 容器启动时创建，容器关闭时销毁
    // 整个应用程序生命周期内只有一个实例
}
```

### 2. Prototype 作用域

```java
@Service
@Scope("prototype")
public class PrototypeService {
    // 每次获取都创建新实例
    // Spring 不管理销毁，需要手动处理
}
```

### 3. Web 作用域

```java
@Service
@Scope("request")
public class RequestScopedService {
    // 每个 HTTP 请求创建一个实例
    // 请求结束时销毁
}

@Service
@Scope("session")
public class SessionScopedService {
    // 每个 HTTP 会话创建一个实例
    // 会话结束时销毁
}
```

## 🎯 最佳实践

### 1. 合理使用初始化方法

```java
// ✅ 推荐：使用 @PostConstruct 进行简单初始化
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void init() {
        // 简单的初始化逻辑
        System.out.println("UserService 初始化完成");
    }
}
```

### 2. 正确处理资源清理

```java
// ✅ 推荐：使用 @PreDestroy 清理资源
@Service
public class FileService {
    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(10);
    }

    @PreDestroy
    public void cleanup() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
```

### 3. 避免在构造函数中进行复杂操作

```java
// ❌ 避免：在构造函数中进行复杂操作
@Service
public class BadService {
    public BadService() {
        // 不要在构造函数中执行复杂逻辑
        connectToDatabase();
        loadConfiguration();
    }
}

// ✅ 推荐：在初始化方法中进行复杂操作
@Service
public class GoodService {

    public GoodService() {
        // 构造函数保持简单
    }

    @PostConstruct
    public void init() {
        // 在初始化方法中执行复杂逻辑
        connectToDatabase();
        loadConfiguration();
    }
}
```

### 4. 谨慎使用 Aware 接口

```java
// ⚠️ 谨慎使用：只在必要时实现 Aware 接口
@Service
public class ContextAwareService implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    // 只在真正需要动态获取 Bean 时使用
    public void doSomething() {
        SomeBean bean = applicationContext.getBean(SomeBean.class);
        // 使用 bean
    }
}
```

## 📊 性能优化建议

### 1. 延迟初始化

```java
@Service
@Lazy
public class HeavyService {
    // 只有在首次使用时才会执行完整的生命周期
}
```

### 2. 条件化创建

```java
@Service
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
public class ConditionalService {
    // 只有在条件满足时才会创建和初始化
}
```

### 3. 异步初始化

```java
@Service
public class AsyncInitService {

    @PostConstruct
    @Async
    public void asyncInit() {
        // 异步执行耗时的初始化操作
        performHeavyInitialization();
    }
}
```

## 📝 小结

Spring Bean 生命周期提供了完整的扩展点来管理对象的创建和销毁：

- **创建阶段** - 实例化、属性注入、Aware 回调、初始化
- **使用阶段** - Bean 可以被应用程序正常使用
- **销毁阶段** - 资源清理和对象销毁
- **扩展点** - BeanPostProcessor、初始化/销毁方法
- **作用域影响** - 不同作用域有不同的生命周期管理策略

理解 Bean 生命周期有助于：
- 在正确的时机执行初始化和清理逻辑
- 创建自定义的扩展组件
- 优化应用程序的启动和关闭性能
- 调试和排查 Bean 相关问题