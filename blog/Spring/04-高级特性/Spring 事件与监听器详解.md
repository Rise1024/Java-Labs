---
title: Spring 事件与监听器详解
description: Spring 事件与监听器详解
tags: [Spring Event]
category: Spring
date: 2025-09-10
---

# Spring 事件与监听器详解

## 🎯 概述

Spring 框架提供了一套完整的事件驱动机制，基于观察者模式实现，允许应用程序组件之间进行松耦合的通信。通过事件发布和监听机制，我们可以实现业务逻辑的解耦，提高系统的可扩展性和可维护性。

## 📚 事件机制基础

### 核心组件

```
事件源 (Publisher)
    ↓ 发布
ApplicationEvent (事件)
    ↓ 传递
ApplicationContext (事件容器)
    ↓ 分发
ApplicationListener (监听器)
    ↓ 处理
业务逻辑
```

### 基本概念

- **ApplicationEvent** - 所有事件的基类
- **ApplicationListener** - 事件监听器接口
- **ApplicationEventPublisher** - 事件发布器接口
- **ApplicationContext** - 默认的事件发布器实现
- **@EventListener** - 基于注解的事件监听

## 🎪 内置事件类型

### 1. 容器生命周期事件

```java
@Component
public class ContainerEventListener {

    @EventListener
    public void handleContextRefreshed(ContextRefreshedEvent event) {
        System.out.println("容器刷新完成事件");
        ApplicationContext context = event.getApplicationContext();
        String[] beanNames = context.getBeanDefinitionNames();
        System.out.println("容器中Bean数量: " + beanNames.length);
    }

    @EventListener
    public void handleContextStarted(ContextStartedEvent event) {
        System.out.println("容器启动事件");
    }

    @EventListener
    public void handleContextStopped(ContextStoppedEvent event) {
        System.out.println("容器停止事件");
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        System.out.println("容器关闭事件");
        // 执行清理操作
    }
}
```

### 2. 请求相关事件（Web 环境）

```java
@Component
public class RequestEventListener {

    @EventListener
    public void handleRequestHandled(RequestHandledEvent event) {
        System.out.println("请求处理完成事件");
        System.out.println("处理时间: " + event.getProcessingTimeMillis() + "ms");
        System.out.println("状态码: " + event.getStatusCode());
    }

    @EventListener
    public void handleServletRequestHandled(ServletRequestHandledEvent event) {
        System.out.println("Servlet请求处理事件");
        System.out.println("请求URL: " + event.getRequestUrl());
        System.out.println("客户端地址: " + event.getClientAddress());
        System.out.println("方法: " + event.getMethod());
    }
}
```

## 🎨 自定义事件

### 1. 创建自定义事件

```java
// 基于 ApplicationEvent 的事件
public class UserRegisteredEvent extends ApplicationEvent {
    private final User user;
    private final String registrationTime;

    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
        this.registrationTime = LocalDateTime.now().toString();
    }

    public User getUser() {
        return user;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }
}

// POJO 事件（Spring 4.2+）
public class OrderCreatedEvent {
    private final Order order;
    private final String eventTime;
    private final String eventId;

    public OrderCreatedEvent(Order order) {
        this.order = order;
        this.eventTime = LocalDateTime.now().toString();
        this.eventId = UUID.randomUUID().toString();
    }

    // Getters
    public Order getOrder() { return order; }
    public String getEventTime() { return eventTime; }
    public String getEventId() { return eventId; }
}

// 通用事件类
public class GenericEvent<T> {
    private final T data;
    private final String eventType;
    private final long timestamp;

    public GenericEvent(T data, String eventType) {
        this.data = data;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public T getData() { return data; }
    public String getEventType() { return eventType; }
    public long getTimestamp() { return timestamp; }
}
```

### 2. 发布自定义事件

```java
@Service
public class UserService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        // 保存用户
        User savedUser = userRepository.save(user);

        // 发布用户注册事件
        UserRegisteredEvent event = new UserRegisteredEvent(this, savedUser);
        eventPublisher.publishEvent(event);

        // 发布 POJO 事件
        eventPublisher.publishEvent(new OrderCreatedEvent(new Order()));

        // 发布泛型事件
        eventPublisher.publishEvent(new GenericEvent<>(savedUser, "USER_REGISTERED"));

        return savedUser;
    }
}
```

## 🎧 事件监听器

### 1. 基于接口的监听器

```java
@Component
public class UserRegistrationListener implements ApplicationListener<UserRegisteredEvent> {

    @Override
    public void onApplicationEvent(UserRegisteredEvent event) {
        User user = event.getUser();
        System.out.println("用户注册监听器: " + user.getName() + " 已注册");

        // 发送欢迎邮件
        sendWelcomeEmail(user);
    }

    private void sendWelcomeEmail(User user) {
        System.out.println("发送欢迎邮件给: " + user.getEmail());
    }
}

// 泛型监听器
@Component
public class GenericListener implements ApplicationListener<GenericEvent<User>> {

    @Override
    public void onApplicationEvent(GenericEvent<User> event) {
        User user = event.getData();
        String eventType = event.getEventType();
        System.out.println("泛型事件监听: " + eventType + " - " + user.getName());
    }
}
```

### 2. 基于注解的监听器

```java
@Component
public class AnnotationEventListener {

    // 基本事件监听
    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        System.out.println("注解监听器: 用户 " + user.getName() + " 注册成功");
    }

    // 条件监听
    @EventListener(condition = "#event.user.age >= 18")
    public void handleAdultUserRegistered(UserRegisteredEvent event) {
        System.out.println("成年用户注册: " + event.getUser().getName());
    }

    // 多事件类型监听
    @EventListener({UserRegisteredEvent.class, UserUpdatedEvent.class})
    public void handleUserEvents(ApplicationEvent event) {
        System.out.println("用户相关事件: " + event.getClass().getSimpleName());
    }

    // POJO 事件监听
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        Order order = event.getOrder();
        System.out.println("订单创建事件: " + event.getEventId());
    }

    // 异步监听
    @Async
    @EventListener
    public void handleAsyncUserRegistered(UserRegisteredEvent event) throws InterruptedException {
        Thread.sleep(2000); // 模拟耗时操作
        System.out.println("异步处理用户注册: " + event.getUser().getName());
    }

    // 有序监听
    @Order(1)
    @EventListener
    public void handleUserRegisteredFirst(UserRegisteredEvent event) {
        System.out.println("第一个处理器: " + event.getUser().getName());
    }

    @Order(2)
    @EventListener
    public void handleUserRegisteredSecond(UserRegisteredEvent event) {
        System.out.println("第二个处理器: " + event.getUser().getName());
    }
}
```

### 3. 条件化监听

```java
@Component
public class ConditionalEventListener {

    // SpEL 表达式条件
    @EventListener(condition = "#event.user.email.contains('@vip.com')")
    public void handleVipUserRegistered(UserRegisteredEvent event) {
        System.out.println("VIP用户注册: " + event.getUser().getName());
        // VIP用户特殊处理
    }

    // 复杂条件
    @EventListener(condition = "#event.user.age > 18 && #event.user.country == 'CN'")
    public void handleChineseAdultUser(UserRegisteredEvent event) {
        System.out.println("中国成年用户: " + event.getUser().getName());
    }

    // 方法调用条件
    @EventListener(condition = "@userService.isValidUser(#event.user)")
    public void handleValidUser(UserRegisteredEvent event) {
        System.out.println("有效用户: " + event.getUser().getName());
    }
}

@Service
public class UserService {
    public boolean isValidUser(User user) {
        return user != null && user.getName() != null && user.getEmail() != null;
    }
}
```

## 🚀 高级事件处理

### 1. 异步事件处理

```java
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("EventAsync-");
        executor.initialize();
        return executor;
    }
}

@Component
public class AsyncEventListener {

    @Async
    @EventListener
    public void handleUserRegisteredAsync(UserRegisteredEvent event) {
        String threadName = Thread.currentThread().getName();
        System.out.println("异步处理线程: " + threadName);

        try {
            // 模拟耗时操作
            Thread.sleep(3000);
            sendWelcomeEmail(event.getUser());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendWelcomeEmail(User user) {
        System.out.println("异步发送欢迎邮件给: " + user.getEmail());
    }
}
```

### 2. 事务性事件监听

```java
@Component
public class TransactionalEventListener {

    // 事务提交后执行
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredAfterCommit(UserRegisteredEvent event) {
        System.out.println("事务提交后处理: " + event.getUser().getName());
        // 发送通知、更新缓存等操作
    }

    // 事务回滚后执行
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleUserRegisteredAfterRollback(UserRegisteredEvent event) {
        System.out.println("事务回滚后处理: " + event.getUser().getName());
        // 清理操作
    }

    // 事务完成后执行（无论提交还是回滚）
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    public void handleUserRegisteredAfterCompletion(UserRegisteredEvent event) {
        System.out.println("事务完成后处理: " + event.getUser().getName());
    }

    // 事务提交前执行
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleUserRegisteredBeforeCommit(UserRegisteredEvent event) {
        System.out.println("事务提交前处理: " + event.getUser().getName());
    }
}
```

### 3. 自定义事件多播器

```java
@Configuration
public class EventConfig {

    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();

        // 设置异步执行器
        multicaster.setTaskExecutor(new SimpleAsyncTaskExecutor());

        // 设置错误处理器
        multicaster.setErrorHandler(throwable -> {
            System.err.println("事件处理异常: " + throwable.getMessage());
            throwable.printStackTrace();
        });

        return multicaster;
    }
}

// 自定义多播器
public class CustomApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    @Override
    public void multicastEvent(ApplicationEvent event) {
        System.out.println("自定义多播器处理事件: " + event.getClass().getSimpleName());
        super.multicastEvent(event);
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        System.out.println("处理类型化事件: " + eventType);
        super.multicastEvent(event, eventType);
    }
}
```

## 🎯 实际应用场景

### 1. 用户管理系统

```java
// 用户相关事件
public class UserRegisteredEvent extends ApplicationEvent {
    private final User user;

    public UserRegisteredEvent(Object source, User user) {
        super(source);
        this.user = user;
    }

    public User getUser() { return user; }
}

public class UserUpdatedEvent extends ApplicationEvent {
    private final User user;
    private final Map<String, Object> changes;

    public UserUpdatedEvent(Object source, User user, Map<String, Object> changes) {
        super(source);
        this.user = user;
        this.changes = changes;
    }

    public User getUser() { return user; }
    public Map<String, Object> getChanges() { return changes; }
}

// 用户服务
@Service
@Transactional
public class UserService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private UserRepository userRepository;

    public User registerUser(User user) {
        User savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(this, savedUser));
        return savedUser;
    }

    public User updateUser(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId);
        // 应用更新
        User updatedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserUpdatedEvent(this, updatedUser, updates));
        return updatedUser;
    }
}

// 事件监听器
@Component
public class UserEventHandlers {

    @Async
    @EventListener
    public void sendWelcomeEmail(UserRegisteredEvent event) {
        // 发送欢迎邮件
        emailService.sendWelcomeEmail(event.getUser());
    }

    @EventListener
    public void updateUserStatistics(UserRegisteredEvent event) {
        // 更新用户统计
        statisticsService.incrementUserCount();
    }

    @EventListener
    public void invalidateUserCache(UserUpdatedEvent event) {
        // 清除用户缓存
        cacheService.evictUser(event.getUser().getId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyThirdPartyService(UserRegisteredEvent event) {
        // 通知第三方服务
        thirdPartyService.notifyUserRegistration(event.getUser());
    }
}
```

### 2. 订单处理系统

```java
// 订单事件
public class OrderCreatedEvent {
    private final Order order;
    private final Customer customer;

    public OrderCreatedEvent(Order order, Customer customer) {
        this.order = order;
        this.customer = customer;
    }

    public Order getOrder() { return order; }
    public Customer getCustomer() { return customer; }
}

public class OrderPaidEvent {
    private final Order order;
    private final Payment payment;

    public OrderPaidEvent(Order order, Payment payment) {
        this.order = order;
        this.payment = payment;
    }

    public Order getOrder() { return order; }
    public Payment getPayment() { return payment; }
}

public class OrderShippedEvent {
    private final Order order;
    private final String trackingNumber;

    public OrderShippedEvent(Order order, String trackingNumber) {
        this.order = order;
        this.trackingNumber = trackingNumber;
    }

    public Order getOrder() { return order; }
    public String getTrackingNumber() { return trackingNumber; }
}

// 订单服务
@Service
@Transactional
public class OrderService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Order createOrder(Order order, Customer customer) {
        Order savedOrder = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderCreatedEvent(savedOrder, customer));
        return savedOrder;
    }

    public void processPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId);
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderPaidEvent(order, payment));
    }

    public void shipOrder(Long orderId, String trackingNumber) {
        Order order = orderRepository.findById(orderId);
        order.setStatus(OrderStatus.SHIPPED);
        order.setTrackingNumber(trackingNumber);
        orderRepository.save(order);
        eventPublisher.publishEvent(new OrderShippedEvent(order, trackingNumber));
    }
}

// 订单事件处理器
@Component
public class OrderEventHandlers {

    @EventListener
    public void reserveInventory(OrderCreatedEvent event) {
        inventoryService.reserveItems(event.getOrder().getItems());
    }

    @EventListener
    public void sendOrderConfirmation(OrderCreatedEvent event) {
        emailService.sendOrderConfirmation(event.getCustomer(), event.getOrder());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateInventory(OrderPaidEvent event) {
        inventoryService.reduceStock(event.getOrder().getItems());
    }

    @EventListener
    public void sendPaymentReceipt(OrderPaidEvent event) {
        emailService.sendPaymentReceipt(event.getOrder(), event.getPayment());
    }

    @EventListener
    public void sendShippingNotification(OrderShippedEvent event) {
        smsService.sendShippingNotification(
            event.getOrder().getCustomer().getPhone(),
            event.getTrackingNumber()
        );
    }

    @Async
    @EventListener
    public void updateRecommendations(OrderPaidEvent event) {
        recommendationService.updateCustomerProfile(
            event.getOrder().getCustomer(),
            event.getOrder().getItems()
        );
    }
}
```

### 3. 审计日志系统

```java
// 审计事件
public class AuditEvent {
    private final String entityType;
    private final String entityId;
    private final String operation;
    private final String userId;
    private final Map<String, Object> changes;
    private final LocalDateTime timestamp;

    public AuditEvent(String entityType, String entityId, String operation,
                     String userId, Map<String, Object> changes) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.operation = operation;
        this.userId = userId;
        this.changes = changes;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
}

// 审计注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String entityType();
    String operation();
}

// 审计切面
@Aspect
@Component
public class AuditAspect {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed();

        // 获取实体ID和用户ID
        String entityId = extractEntityId(joinPoint, result);
        String userId = getCurrentUserId();

        // 发布审计事件
        AuditEvent auditEvent = new AuditEvent(
            auditable.entityType(),
            entityId,
            auditable.operation(),
            userId,
            extractChanges(joinPoint.getArgs())
        );

        eventPublisher.publishEvent(auditEvent);

        return result;
    }

    private String extractEntityId(ProceedingJoinPoint joinPoint, Object result) {
        // 提取实体ID的逻辑
        return "entity-id";
    }

    private String getCurrentUserId() {
        // 获取当前用户ID的逻辑
        return "user-id";
    }

    private Map<String, Object> extractChanges(Object[] args) {
        // 提取变更信息的逻辑
        return new HashMap<>();
    }
}

// 审计服务
@Service
public class UserService {

    @Auditable(entityType = "User", operation = "CREATE")
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Auditable(entityType = "User", operation = "UPDATE")
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Auditable(entityType = "User", operation = "DELETE")
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}

// 审计事件监听器
@Component
public class AuditEventListener {

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        // 记录到数据库
        auditLogRepository.save(new AuditLog(event));

        // 发送到消息队列
        messageQueue.send("audit.queue", event);

        // 检查敏感操作
        if (isSensitiveOperation(event)) {
            alertService.sendSecurityAlert(event);
        }
    }

    private boolean isSensitiveOperation(AuditEvent event) {
        return "DELETE".equals(event.getOperation()) ||
               "Admin".equals(event.getEntityType());
    }
}
```

## 🎯 最佳实践

### 1. 事件设计原则

```java
// ✅ 推荐：事件包含完整的上下文信息
public class UserRegisteredEvent {
    private final User user;
    private final String registrationSource; // 注册来源
    private final LocalDateTime timestamp;
    private final Map<String, Object> metadata; // 元数据

    // 构造函数和 Getters
}

// ❌ 避免：事件信息不完整
public class UserRegisteredEvent {
    private final Long userId; // 只有ID，需要额外查询
}
```

### 2. 监听器异常处理

```java
@Component
public class RobustEventListener {

    @EventListener
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            // 业务逻辑
            processUserRegistration(event.getUser());
        } catch (Exception e) {
            // 记录错误但不抛出，避免影响其他监听器
            logger.error("处理用户注册事件失败", e);

            // 可以发布错误事件进行补偿
            eventPublisher.publishEvent(new UserRegistrationFailedEvent(event, e));
        }
    }
}
```

### 3. 事件版本控制

```java
// 事件版本化
public abstract class VersionedEvent {
    private final String version;

    protected VersionedEvent(String version) {
        this.version = version;
    }

    public String getVersion() { return version; }
}

public class UserRegisteredEventV1 extends VersionedEvent {
    public UserRegisteredEventV1(User user) {
        super("1.0");
        // V1 字段
    }
}

public class UserRegisteredEventV2 extends VersionedEvent {
    public UserRegisteredEventV2(User user, String registrationSource) {
        super("2.0");
        // V2 字段
    }
}

// 版本兼容的监听器
@EventListener
public void handleUserRegistered(VersionedEvent event) {
    switch (event.getVersion()) {
        case "1.0":
            handleV1Event((UserRegisteredEventV1) event);
            break;
        case "2.0":
            handleV2Event((UserRegisteredEventV2) event);
            break;
        default:
            logger.warn("不支持的事件版本: " + event.getVersion());
    }
}
```

### 4. 性能优化

```java
@Configuration
public class EventPerformanceConfig {

    // 配置异步执行器
    @Bean
    public TaskExecutor eventTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors());
        executor.setMaxPoolSize(Runtime.getRuntime().availableProcessors() * 2);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("Event-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    // 自定义事件多播器以提高性能
    @Bean
    public ApplicationEventMulticaster applicationEventMulticaster() {
        SimpleApplicationEventMulticaster multicaster = new SimpleApplicationEventMulticaster();
        multicaster.setTaskExecutor(eventTaskExecutor());
        return multicaster;
    }
}
```

## 📝 小结

Spring 事件机制提供了强大的松耦合通信能力：

- **内置事件** - 容器生命周期、请求处理事件
- **自定义事件** - ApplicationEvent、POJO事件、泛型事件
- **监听方式** - 接口实现、@EventListener注解
- **高级特性** - 异步处理、事务性监听、条件监听
- **实际应用** - 用户管理、订单处理、审计日志
- **最佳实践** - 异常处理、版本控制、性能优化

事件驱动架构是构建可扩展、可维护系统的重要模式。合理使用Spring事件机制可以实现业务组件的解耦，提高系统的响应性和可扩展性。在设计事件时要注意事件的完整性、监听器的健壮性，以及异步处理的性能优化。