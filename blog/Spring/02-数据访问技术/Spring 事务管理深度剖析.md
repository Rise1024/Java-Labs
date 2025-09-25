---
title: Spring 事务管理深度剖析
description: Spring 事务管理深度剖析
tags: [Spring Transaction, 事务管理]
category: Spring
date: 2025-09-25
---

# Spring 事务管理深度剖析

## 🎯 概述

Spring 事务管理是企业级应用开发的核心技术之一，它提供了一套完整、统一的事务处理抽象层。Spring 的事务管理不仅支持传统的 JDBC 事务，还能无缝集成 JPA、Hibernate、JTA 等多种事务技术，为开发者提供了声明式和编程式两种事务管理方式。本文将深入剖析 Spring 事务管理的核心原理、最佳实践以及在复杂企业环境中的应用策略。

## 🏗️ Spring 事务管理架构深度解析

### 1. 事务管理核心架构

Spring 事务管理采用了策略模式和模板方法模式，通过统一的抽象接口支持多种事务管理技术。

```
Spring 事务管理架构图

┌─────────────────────────────────────────────────────────────┐
│                    应用层 (Application Layer)                │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │   @Transactional │  │ TransactionTemplate │              │
│  │   声明式事务      │  │    编程式事务     │                │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                   事务抽象层 (Transaction Abstraction)        │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │          PlatformTransactionManager                     │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │TransactionDefinition│TransactionStatus│TransactionException│ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                  事务实现层 (Transaction Implementation)       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │DataSourceTx │ │   JpaTx     │ │   JtaTx     │           │
│  │  Manager    │ │  Manager    │ │  Manager    │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    资源层 (Resource Layer)                   │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   JDBC      │ │     JPA     │ │     JTA     │           │
│  │ DataSource  │ │EntityManager│ │UserTransaction│          │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### 2. 核心接口深度解析

#### PlatformTransactionManager 接口架构

```java
/**
 * Spring 事务管理的核心接口
 * 采用策略模式，支持多种事务管理实现
 */
public interface PlatformTransactionManager extends TransactionManager {
    
    /**
     * 根据事务定义获取事务状态
     * @param definition 事务定义，包含传播行为、隔离级别、超时等配置
     * @return 事务状态对象，包含事务的当前状态信息
     * @throws TransactionException 事务异常
     */
    TransactionStatus getTransaction(@Nullable TransactionDefinition definition) 
        throws TransactionException;
    
    /**
     * 提交事务
     * @param status 事务状态对象
     * @throws TransactionException 提交失败时抛出异常
     */
    void commit(TransactionStatus status) throws TransactionException;
    
    /**
     * 回滚事务
     * @param status 事务状态对象
     * @throws TransactionException 回滚失败时抛出异常
     */
    void rollback(TransactionStatus status) throws TransactionException;
}

/**
 * 响应式事务管理器接口
 * 支持非阻塞的响应式事务处理
 */
public interface ReactiveTransactionManager extends TransactionManager {
    
    /**
     * 获取响应式事务
     * @param definition 事务定义
     * @return 包含事务信息的 Mono
     */
    Mono<ReactiveTransaction> getReactiveTransaction(@Nullable TransactionDefinition definition);
    
    /**
     * 提交响应式事务
     * @param transaction 响应式事务对象
     * @return 表示提交操作完成的 Mono
     */
    Mono<Void> commit(ReactiveTransaction transaction);
    
    /**
     * 回滚响应式事务
     * @param transaction 响应式事务对象
     * @return 表示回滚操作完成的 Mono
     */
    Mono<Void> rollback(ReactiveTransaction transaction);
}
```

#### TransactionDefinition 事务定义详解

```java
/**
 * 事务定义接口，定义了事务的各种属性
 */
public interface TransactionDefinition {
    
    // 事务传播行为常量
    int PROPAGATION_REQUIRED = 0;      // 如果存在事务则加入，否则创建新事务
    int PROPAGATION_SUPPORTS = 1;      // 如果存在事务则加入，否则以非事务方式执行
    int PROPAGATION_MANDATORY = 2;     // 必须在事务中执行，否则抛出异常
    int PROPAGATION_REQUIRES_NEW = 3;  // 总是创建新事务，挂起当前事务
    int PROPAGATION_NOT_SUPPORTED = 4; // 以非事务方式执行，挂起当前事务
    int PROPAGATION_NEVER = 5;         // 以非事务方式执行，如果存在事务则抛出异常
    int PROPAGATION_NESTED = 6;        // 嵌套事务，基于保存点实现
    
    // 事务隔离级别常量
    int ISOLATION_DEFAULT = -1;           // 使用数据库默认隔离级别
    int ISOLATION_READ_UNCOMMITTED = 1;   // 读未提交
    int ISOLATION_READ_COMMITTED = 2;     // 读已提交
    int ISOLATION_REPEATABLE_READ = 4;    // 可重复读
    int ISOLATION_SERIALIZABLE = 8;       // 序列化
    
    // 默认超时时间
    int TIMEOUT_DEFAULT = -1;
    
    /**
     * 获取事务传播行为
     */
    default int getPropagationBehavior() {
        return PROPAGATION_REQUIRED;
    }
    
    /**
     * 获取事务隔离级别
     */
    default int getIsolationLevel() {
        return ISOLATION_DEFAULT;
    }
    
    /**
     * 获取事务超时时间（秒）
     */
    default int getTimeout() {
        return TIMEOUT_DEFAULT;
    }
    
    /**
     * 是否为只读事务
     */
    default boolean isReadOnly() {
        return false;
    }
    
    /**
     * 获取事务名称
     */
    @Nullable
    default String getName() {
        return null;
    }
}

/**
 * 默认事务定义实现
 */
public class DefaultTransactionDefinition implements TransactionDefinition, Serializable {
    
    private int propagationBehavior = PROPAGATION_REQUIRED;
    private int isolationLevel = ISOLATION_DEFAULT;
    private int timeout = TIMEOUT_DEFAULT;
    private boolean readOnly = false;
    @Nullable
    private String name;
    
    // 构造函数
    public DefaultTransactionDefinition() {}
    
    public DefaultTransactionDefinition(int propagationBehavior) {
        this.propagationBehavior = propagationBehavior;
    }
    
    public DefaultTransactionDefinition(TransactionDefinition other) {
        this.propagationBehavior = other.getPropagationBehavior();
        this.isolationLevel = other.getIsolationLevel();
        this.timeout = other.getTimeout();
        this.readOnly = other.isReadOnly();
        this.name = other.getName();
    }
    
    // Getters and Setters with validation
    public void setPropagationBehavior(int propagationBehavior) {
        if (!constants.contains(propagationBehavior)) {
            throw new IllegalArgumentException("Only values of propagation constants allowed");
        }
        this.propagationBehavior = propagationBehavior;
    }
    
    public void setIsolationLevel(int isolationLevel) {
        if (!constants.contains(isolationLevel)) {
            throw new IllegalArgumentException("Only values of isolation constants allowed");
        }
        this.isolationLevel = isolationLevel;
    }
    
    // 其他 setter 方法...
}
```

### 3. 事务管理器实现详解

#### DataSourceTransactionManager - JDBC 事务管理

```java
/**
 * 基于 DataSource 的事务管理器实现
 * 适用于纯 JDBC 和 MyBatis 等基于 JDBC 的持久化技术
 */
public class DataSourceTransactionManager extends AbstractPlatformTransactionManager 
        implements ResourceTransactionManager, InitializingBean {
    
    @Nullable
    private DataSource dataSource;
    
    private boolean enforceReadOnly = false;
    
    public DataSourceTransactionManager() {
        setNestedTransactionAllowed(true);
    }
    
    public DataSourceTransactionManager(DataSource dataSource) {
        this();
        setDataSource(dataSource);
        afterPropertiesSet();
    }
    
    @Override
    protected Object doGetTransaction() throws TransactionException {
        DataSourceTransactionObject txObject = new DataSourceTransactionObject();
        txObject.setSavepointAllowed(isNestedTransactionAllowed());
        
        // 从事务同步管理器获取连接持有者
        ConnectionHolder conHolder = 
            (ConnectionHolder) TransactionSynchronizationManager.getResource(obtainDataSource());
        txObject.setConnectionHolder(conHolder, false);
        
        return txObject;
    }
    
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        return (txObject.hasConnectionHolder() && txObject.getConnectionHolder().isTransactionActive());
    }
    
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) 
            throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) transaction;
        Connection con = null;
        
        try {
            if (!txObject.hasConnectionHolder() || 
                txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
                Connection newCon = obtainDataSource().getConnection();
                txObject.setConnectionHolder(new ConnectionHolder(newCon), true);
            }
            
            txObject.getConnectionHolder().setSynchronizedWithTransaction(true);
            con = txObject.getConnectionHolder().getConnection();
            
            // 设置只读和隔离级别
            Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(con, definition);
            txObject.setPreviousIsolationLevel(previousIsolationLevel);
            txObject.setReadOnly(definition.isReadOnly());
            
            // 关闭自动提交
            if (con.getAutoCommit()) {
                txObject.setMustRestoreAutoCommit(true);
                con.setAutoCommit(false);
            }
            
            prepareTransactionalConnection(con, definition);
            txObject.getConnectionHolder().setTransactionActive(true);
            
            // 设置超时
            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getConnectionHolder().setTimeoutInSeconds(timeout);
            }
            
            // 绑定连接到当前线程
            if (txObject.isNewConnectionHolder()) {
                TransactionSynchronizationManager.bindResource(obtainDataSource(), 
                    txObject.getConnectionHolder());
            }
        } catch (Throwable ex) {
            if (txObject.isNewConnectionHolder()) {
                DataSourceUtils.releaseConnection(con, obtainDataSource());
                txObject.setConnectionHolder(null, false);
            }
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", ex);
        }
    }
    
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        
        try {
            con.commit();
        } catch (SQLException ex) {
            throw translateException("JDBC commit", ex);
        }
    }
    
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        DataSourceTransactionObject txObject = (DataSourceTransactionObject) status.getTransaction();
        Connection con = txObject.getConnectionHolder().getConnection();
        
        try {
            con.rollback();
        } catch (SQLException ex) {
            throw translateException("JDBC rollback", ex);
        }
    }
    
    // 事务对象内部类
    private static class DataSourceTransactionObject extends JdbcTransactionObjectSupport {
        
        private boolean newConnectionHolder;
        private boolean mustRestoreAutoCommit;
        
        public void setConnectionHolder(@Nullable ConnectionHolder connectionHolder, boolean newConnectionHolder) {
            super.setConnectionHolder(connectionHolder);
            this.newConnectionHolder = newConnectionHolder;
        }
        
        public boolean isNewConnectionHolder() {
            return this.newConnectionHolder;
        }
        
        // 其他方法...
    }
}
```

#### JpaTransactionManager - JPA 事务管理

```java
/**
 * JPA 事务管理器实现
 * 支持 JPA EntityManager 的事务管理
 */
public class JpaTransactionManager extends AbstractPlatformTransactionManager 
        implements ResourceTransactionManager, BeanFactoryAware, InitializingBean {
    
    @Nullable
    private EntityManagerFactory entityManagerFactory;
    
    @Nullable
    private String persistenceUnitName;
    
    private final Map<String, Object> jpaPropertyMap = new HashMap<>();
    
    @Nullable
    private EntityManagerFactory beanFactory;
    
    public JpaTransactionManager() {
        setNestedTransactionAllowed(true);
    }
    
    public JpaTransactionManager(EntityManagerFactory emf) {
        this();
        setEntityManagerFactory(emf);
        afterPropertiesSet();
    }
    
    @Override
    protected Object doGetTransaction() throws TransactionException {
        JpaTransactionObject txObject = new JpaTransactionObject();
        txObject.setSavepointAllowed(isNestedTransactionAllowed());
        
        EntityManagerHolder emHolder = (EntityManagerHolder) 
            TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
        if (emHolder != null) {
            txObject.setEntityManagerHolder(emHolder, false);
        }
        
        if (getDataSource() != null) {
            ConnectionHolder conHolder = (ConnectionHolder) 
                TransactionSynchronizationManager.getResource(getDataSource());
            if (conHolder != null) {
                txObject.setConnectionHolder(conHolder);
            }
        }
        
        return txObject;
    }
    
    @Override
    protected boolean isExistingTransaction(Object transaction) throws TransactionException {
        JpaTransactionObject txObject = (JpaTransactionObject) transaction;
        return (txObject.hasEntityManagerHolder() && 
                txObject.getEntityManagerHolder().isTransactionActive());
    }
    
    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) 
            throws TransactionException {
        JpaTransactionObject txObject = (JpaTransactionObject) transaction;
        
        try {
            if (!txObject.hasEntityManagerHolder() || 
                txObject.getEntityManagerHolder().isSynchronizedWithTransaction()) {
                EntityManager newEm = createEntityManagerForTransaction();
                txObject.setEntityManagerHolder(new EntityManagerHolder(newEm), true);
            }
            
            EntityManager em = txObject.getEntityManagerHolder().getEntityManager();
            
            // 开始 JPA 事务
            final EntityTransaction tx = em.getTransaction();
            tx.begin();
            
            // 设置事务属性
            txObject.getEntityManagerHolder().setTransactionActive(true);
            
            // 设置超时
            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getEntityManagerHolder().setTimeoutInSeconds(timeout);
            }
            
            // 绑定到当前线程
            if (txObject.isNewEntityManagerHolder()) {
                TransactionSynchronizationManager.bindResource(
                    obtainEntityManagerFactory(), txObject.getEntityManagerHolder());
            }
            
            // 处理数据源事务
            if (getDataSource() != null && !txObject.hasConnectionHolder()) {
                Connection con = DataSourceUtils.getConnection(getDataSource());
                txObject.setConnectionHolder(new ConnectionHolder(con));
            }
            
        } catch (Throwable ex) {
            throw new CannotCreateTransactionException("Could not open JPA EntityManager for transaction", ex);
        }
    }
    
    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {
        JpaTransactionObject txObject = (JpaTransactionObject) status.getTransaction();
        EntityTransaction tx = txObject.getEntityManagerHolder().getEntityManager().getTransaction();
        
        try {
            tx.commit();
        } catch (PersistenceException ex) {
            throw translateException("JPA commit", ex);
        }
    }
    
    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        JpaTransactionObject txObject = (JpaTransactionObject) status.getTransaction();
        EntityTransaction tx = txObject.getEntityManagerHolder().getEntityManager().getTransaction();
        
        try {
            if (tx.isActive()) {
                tx.rollback();
            }
        } catch (PersistenceException ex) {
            logger.debug("Could not roll back JPA transaction", ex);
        }
    }
    
    /**
     * 创建用于事务的 EntityManager
     */
    protected EntityManager createEntityManagerForTransaction() {
        EntityManagerFactory emf = obtainEntityManagerFactory();
        Map<String, Object> properties = getJpaPropertyMap();
        return (!CollectionUtils.isEmpty(properties) ? 
               emf.createEntityManager(properties) : emf.createEntityManager());
    }
    
    // JPA 事务对象
    private static class JpaTransactionObject extends JdbcTransactionObjectSupport {
        
        @Nullable
        private EntityManagerHolder entityManagerHolder;
        private boolean newEntityManagerHolder;
        
        public void setEntityManagerHolder(@Nullable EntityManagerHolder entityManagerHolder, 
                                         boolean newEntityManagerHolder) {
            this.entityManagerHolder = entityManagerHolder;
            this.newEntityManagerHolder = newEntityManagerHolder;
        }
        
        public EntityManagerHolder getEntityManagerHolder() {
            Assert.state(this.entityManagerHolder != null, "No EntityManagerHolder available");
            return this.entityManagerHolder;
        }
        
        public boolean hasEntityManagerHolder() {
            return (this.entityManagerHolder != null);
        }
        
        public boolean isNewEntityManagerHolder() {
            return this.newEntityManagerHolder;
        }
    }
}
```

## 🎯 事务传播行为深度分析

### 1. 传播行为详细解析

事务传播行为决定了在一个事务方法调用另一个事务方法时的行为策略。

#### REQUIRED - 最常用的传播行为

```java
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * REQUIRED 传播行为示例
     * 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public User createUser(CreateUserRequest request) {
        // 1. 创建用户
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        User savedUser = userRepository.save(user);
        
        // 2. 记录审计日志（使用相同事务）
        auditService.logUserCreation(savedUser.getId()); // 加入当前事务
        
        // 3. 如果任何一步失败，整个操作都会回滚
        return savedUser;
    }
}

@Service
public class AuditService {
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Transactional(propagation = Propagation.REQUIRED)
    public void logUserCreation(Long userId) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction("USER_CREATED");
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
        
        // 如果这里抛出异常，整个 createUser 事务都会回滚
    }
}
```

#### REQUIRES_NEW - 独立事务

```java
@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Transactional
    public Order processOrder(CreateOrderRequest request) {
        // 主事务：处理订单
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.PROCESSING);
        
        try {
            // 扣减库存（独立事务）
            inventoryService.decreaseStock(request.getProductId(), request.getQuantity());
            
            order.setStatus(OrderStatus.CONFIRMED);
            Order savedOrder = orderRepository.save(order);
            
            // 发送通知（独立事务，即使失败也不影响订单）
            notificationService.sendOrderConfirmation(savedOrder.getId());
            
            return savedOrder;
            
        } catch (InsufficientStockException e) {
            order.setStatus(OrderStatus.FAILED);
            orderRepository.save(order);
            throw e;
        }
    }
}

@Service
public class InventoryService {
    
    @Autowired
    private InventoryRepository inventoryRepository;
    
    /**
     * REQUIRES_NEW 传播行为
     * 总是创建新事务，挂起当前事务
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseStock(Long productId, int quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        
        if (inventory.getStock() < quantity) {
            throw new InsufficientStockException("库存不足");
        }
        
        inventory.setStock(inventory.getStock() - quantity);
        inventoryRepository.save(inventory);
        
        // 这个事务独立提交，即使外部事务回滚，库存扣减仍然有效
    }
}

@Service
public class NotificationService {
    
    @Autowired
    private EmailService emailService;
    
    /**
     * 通知服务使用独立事务
     * 即使通知发送失败，也不应该影响订单处理
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendOrderConfirmation(Long orderId) {
        try {
            emailService.sendOrderConfirmationEmail(orderId);
        } catch (Exception e) {
            // 记录日志但不抛出异常，避免影响主事务
            logger.error("发送订单确认邮件失败: orderId={}", orderId, e);
        }
    }
}
```

#### NESTED - 嵌套事务

```java
@Service
public class BatchProcessingService {
    
    @Autowired
    private DataProcessor dataProcessor;
    
    @Autowired
    private ErrorHandler errorHandler;
    
    /**
     * 批量处理示例
     * 使用嵌套事务处理部分失败的场景
     */
    @Transactional
    public BatchProcessingResult processBatch(List<DataRecord> records) {
        BatchProcessingResult result = new BatchProcessingResult();
        
        for (DataRecord record : records) {
            try {
                // 使用嵌套事务处理单条记录
                dataProcessor.processRecord(record);
                result.addSuccess(record.getId());
                
            } catch (DataProcessingException e) {
                // 嵌套事务回滚，但主事务继续
                result.addFailure(record.getId(), e.getMessage());
                
                // 记录错误但不影响批处理继续
                errorHandler.handleProcessingError(record, e);
            }
        }
        
        // 保存批处理结果
        return result;
    }
}

@Service
public class DataProcessor {
    
    @Autowired
    private DataRepository dataRepository;
    
    /**
     * NESTED 传播行为
     * 如果当前存在事务，则在嵌套事务内执行
     * 嵌套事务的回滚不会影响外部事务
     */
    @Transactional(propagation = Propagation.NESTED)
    public void processRecord(DataRecord record) {
        // 数据验证
        validateRecord(record);
        
        // 数据转换
        ProcessedData processedData = transformData(record);
        
        // 保存数据
        dataRepository.save(processedData);
        
        // 如果这里抛出异常，只有这条记录的处理会回滚
        // 不影响批处理中的其他记录
    }
    
    private void validateRecord(DataRecord record) {
        if (record.getData() == null || record.getData().isEmpty()) {
            throw new DataProcessingException("数据为空");
        }
        
        if (!isValidFormat(record.getData())) {
            throw new DataProcessingException("数据格式不正确");
        }
    }
    
    private ProcessedData transformData(DataRecord record) {
        // 复杂的数据转换逻辑
        return new ProcessedData(record);
    }
    
    private boolean isValidFormat(String data) {
        // 数据格式验证逻辑
        return data.matches("^[A-Za-z0-9]+$");
    }
}
```

### 2. 传播行为组合应用

```java
/**
 * 复杂业务场景中的传播行为组合应用
 * 电商订单处理系统
 */
@Service
public class ECommerceOrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Autowired
    private LoggingService loggingService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * 主订单处理流程
     * 使用 REQUIRED 传播行为
     */
    @Transactional(
        propagation = Propagation.REQUIRED,
        isolation = Isolation.READ_COMMITTED,
        timeout = 30,
        rollbackFor = {Exception.class}
    )
    public OrderResult processOrder(OrderRequest request) {
        OrderResult result = new OrderResult();
        
        try {
            // 1. 记录开始处理（独立事务，确保记录）
            loggingService.logOrderProcessingStart(request.getOrderId());
            
            // 2. 创建订单记录
            Order order = createOrderRecord(request);
            result.setOrderId(order.getId());
            
            // 3. 扣减库存（独立事务）
            inventoryService.reserveStock(request.getItems());
            
            // 4. 处理支付（嵌套事务，支持重试）
            PaymentResult paymentResult = paymentService.processPayment(request.getPaymentInfo());
            order.setPaymentId(paymentResult.getPaymentId());
            
            // 5. 确认订单
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            // 6. 发送确认通知（独立事务，失败不影响订单）
            notificationService.sendOrderConfirmation(order.getId());
            
            // 7. 记录成功处理（独立事务）
            loggingService.logOrderProcessingSuccess(order.getId());
            
            result.setSuccess(true);
            return result;
            
        } catch (Exception e) {
            // 记录失败信息（独立事务）
            loggingService.logOrderProcessingFailure(request.getOrderId(), e.getMessage());
            
            result.setSuccess(false);
            result.setErrorMessage(e.getMessage());
            throw e;
        }
    }
    
    private Order createOrderRecord(OrderRequest request) {
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setItems(request.getItems());
        order.setTotalAmount(calculateTotalAmount(request.getItems()));
        order.setStatus(OrderStatus.PROCESSING);
        order.setCreatedAt(LocalDateTime.now());
        
        return orderRepository.save(order);
    }
    
    private BigDecimal calculateTotalAmount(List<OrderItem> items) {
        return items.stream()
                   .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                   .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

@Service
public class PaymentService {
    
    @Autowired
    private PaymentGateway paymentGateway;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * 支付处理 - 使用嵌套事务支持重试
     */
    @Transactional(propagation = Propagation.NESTED)
    @Retryable(value = {PaymentGatewayException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public PaymentResult processPayment(PaymentInfo paymentInfo) {
        // 创建支付记录
        Payment payment = new Payment();
        payment.setAmount(paymentInfo.getAmount());
        payment.setPaymentMethod(paymentInfo.getPaymentMethod());
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setCreatedAt(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);
        
        try {
            // 调用第三方支付网关
            PaymentGatewayResult gatewayResult = paymentGateway.charge(paymentInfo);
            
            // 更新支付状态
            savedPayment.setStatus(PaymentStatus.SUCCESS);
            savedPayment.setGatewayTransactionId(gatewayResult.getTransactionId());
            paymentRepository.save(savedPayment);
            
            return new PaymentResult(savedPayment.getId(), true);
            
        } catch (PaymentGatewayException e) {
            savedPayment.setStatus(PaymentStatus.FAILED);
            savedPayment.setErrorMessage(e.getMessage());
            paymentRepository.save(savedPayment);
            
            throw e; // 触发重试
        }
    }
}

@Service
public class LoggingService {
    
    @Autowired
    private ProcessLogRepository processLogRepository;
    
    /**
     * 日志记录 - 使用 REQUIRES_NEW 确保日志记录不受主事务影响
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderProcessingStart(String orderId) {
        ProcessLog log = new ProcessLog();
        log.setOrderId(orderId);
        log.setProcessType("ORDER_PROCESSING");
        log.setStatus("STARTED");
        log.setTimestamp(LocalDateTime.now());
        
        processLogRepository.save(log);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderProcessingSuccess(Long orderId) {
        ProcessLog log = new ProcessLog();
        log.setOrderId(orderId.toString());
        log.setProcessType("ORDER_PROCESSING");
        log.setStatus("SUCCESS");
        log.setTimestamp(LocalDateTime.now());
        
        processLogRepository.save(log);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderProcessingFailure(String orderId, String errorMessage) {
        ProcessLog log = new ProcessLog();
        log.setOrderId(orderId);
        log.setProcessType("ORDER_PROCESSING");
        log.setStatus("FAILED");
        log.setErrorMessage(errorMessage);
        log.setTimestamp(LocalDateTime.now());
        
        processLogRepository.save(log);
    }
}
```

## 🔒 事务隔离级别深度解析

### 1. 隔离级别与并发问题

数据库事务的隔离级别决定了事务之间的可见性，不同的隔离级别可以解决不同的并发问题。

#### 并发问题详解

```java
/**
 * 并发问题演示服务
 * 用于说明不同隔离级别解决的并发问题
 */
@Service
public class ConcurrencyDemoService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private TransactionLogRepository transactionLogRepository;
    
    /**
     * 脏读演示 - READ_UNCOMMITTED 隔离级别
     * 可能读取到其他事务未提交的数据
     */
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public void demonstrateDirtyRead() {
        // 线程1：修改账户余额但未提交
        Account account = accountRepository.findById(1L).orElseThrow();
        account.setBalance(account.getBalance().add(new BigDecimal("1000")));
        accountRepository.save(account);
        
        // 此时如果线程2读取，可能看到未提交的1000元增加
        // 如果线程1回滚，线程2读取的就是"脏数据"
        
        // 模拟长时间处理
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // 假设这里发生异常，事务回滚
        throw new RuntimeException("模拟异常，触发回滚");
    }
    
    /**
     * 不可重复读演示 - READ_COMMITTED 隔离级别
     * 在同一事务中多次读取同一数据，结果可能不同
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void demonstrateNonRepeatableRead() {
        // 第一次读取
        Account account1 = accountRepository.findById(1L).orElseThrow();
        BigDecimal balance1 = account1.getBalance();
        System.out.println("第一次读取余额: " + balance1);
        
        // 模拟其他事务修改了这个账户的余额
        simulateExternalModification(1L);
        
        // 第二次读取
        Account account2 = accountRepository.findById(1L).orElseThrow();
        BigDecimal balance2 = account2.getBalance();
        System.out.println("第二次读取余额: " + balance2);
        
        // balance1 和 balance2 可能不同，这就是不可重复读
        if (!balance1.equals(balance2)) {
            System.out.println("发生了不可重复读");
        }
    }
    
    /**
     * 幻读演示 - REPEATABLE_READ 隔离级别
     * 在同一事务中多次查询，结果集的数量可能发生变化
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void demonstratePhantomRead() {
        // 第一次查询：获取余额大于1000的账户
        List<Account> accounts1 = accountRepository.findByBalanceGreaterThan(new BigDecimal("1000"));
        int count1 = accounts1.size();
        System.out.println("第一次查询结果数量: " + count1);
        
        // 模拟其他事务插入了新的账户
        simulateExternalInsertion();
        
        // 第二次查询：同样的条件
        List<Account> accounts2 = accountRepository.findByBalanceGreaterThan(new BigDecimal("1000"));
        int count2 = accounts2.size();
        System.out.println("第二次查询结果数量: " + count2);
        
        // count1 和 count2 可能不同，这就是幻读
        if (count1 != count2) {
            System.out.println("发生了幻读");
        }
    }
    
    /**
     * 串行化隔离级别 - SERIALIZABLE
     * 最高隔离级别，避免所有并发问题，但性能最低
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void demonstrateSerializableIsolation() {
        // 在串行化隔离级别下，事务是完全隔离的
        // 避免了脏读、不可重复读和幻读
        // 但是性能代价很高，容易导致死锁
        
        List<Account> accounts = accountRepository.findAll();
        
        for (Account account : accounts) {
            // 复杂的业务逻辑
            processAccount(account);
        }
        
        // 在此隔离级别下，其他事务无法同时访问这些数据
    }
    
    private void simulateExternalModification(Long accountId) {
        // 模拟其他事务的修改
        // 实际实现中这会是另一个事务
    }
    
    private void simulateExternalInsertion() {
        // 模拟其他事务插入数据
        // 实际实现中这会是另一个事务
    }
    
    private void processAccount(Account account) {
        // 模拟复杂的账户处理逻辑
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### 2. 隔离级别选择策略

```java
/**
 * 基于业务场景的隔离级别选择策略
 */
@Service
public class IsolationLevelStrategyService {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ReportRepository reportRepository;
    
    /**
     * 金融转账 - 使用 SERIALIZABLE 隔离级别
     * 确保数据完全一致，避免任何并发问题
     */
    @Transactional(
        isolation = Isolation.SERIALIZABLE,
        timeout = 30,
        rollbackFor = Exception.class
    )
    public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        // 获取源账户和目标账户
        Account fromAccount = accountRepository.findById(fromAccountId)
            .orElseThrow(() -> new AccountNotFoundException("源账户不存在"));
        Account toAccount = accountRepository.findById(toAccountId)
            .orElseThrow(() -> new AccountNotFoundException("目标账户不存在"));
        
        // 验证余额
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("余额不足");
        }
        
        // 执行转账
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        // 保存更改
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        // 记录转账日志
        createTransferLog(fromAccountId, toAccountId, amount);
    }
    
    /**
     * 订单处理 - 使用 READ_COMMITTED 隔离级别
     * 平衡一致性和性能，适合大多数业务场景
     */
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        timeout = 20,
        rollbackFor = Exception.class
    )
    public Order processOrder(CreateOrderRequest request) {
        // 创建订单
        Order order = new Order();
        order.setCustomerId(request.getCustomerId());
        order.setProductId(request.getProductId());
        order.setQuantity(request.getQuantity());
        order.setStatus(OrderStatus.PROCESSING);
        
        // 检查库存（读已提交的数据）
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new ProductNotFoundException("产品不存在"));
        
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException("库存不足");
        }
        
        // 计算金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(request.getQuantity()));
        order.setTotalAmount(totalAmount);
        
        // 扣减库存
        product.setStock(product.getStock() - request.getQuantity());
        productRepository.save(product);
        
        // 保存订单
        Order savedOrder = orderRepository.save(order);
        
        return savedOrder;
    }
    
    /**
     * 数据报表生成 - 使用 READ_UNCOMMITTED 隔离级别
     * 对数据一致性要求不高，优先考虑性能
     */
    @Transactional(
        isolation = Isolation.READ_UNCOMMITTED,
        readOnly = true,
        timeout = 60
    )
    public SalesReport generateSalesReport(LocalDate startDate, LocalDate endDate) {
        // 对于报表生成，可以容忍读取到未提交的数据
        // 因为报表通常只是趋势分析，不需要绝对精确
        
        List<Order> orders = orderRepository.findByDateRange(startDate, endDate);
        
        SalesReport report = new SalesReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(LocalDateTime.now());
        
        // 计算销售统计
        BigDecimal totalSales = orders.stream()
            .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.setTotalSales(totalSales);
        report.setOrderCount(orders.size());
        
        // 保存报表
        reportRepository.save(report);
        
        return report;
    }
    
    /**
     * 库存盘点 - 使用 REPEATABLE_READ 隔离级别
     * 确保盘点过程中数据不会发生变化
     */
    @Transactional(
        isolation = Isolation.REPEATABLE_READ,
        timeout = 120,
        rollbackFor = Exception.class
    )
    public InventoryReport performInventoryCount() {
        InventoryReport report = new InventoryReport();
        report.setCountDate(LocalDate.now());
        report.setStartTime(LocalDateTime.now());
        
        // 获取所有产品
        List<Product> products = productRepository.findAll();
        List<InventoryItem> inventoryItems = new ArrayList<>();
        
        for (Product product : products) {
            // 在整个盘点过程中，产品数据不会发生变化
            InventoryItem item = new InventoryItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setSystemStock(product.getStock());
            
            // 模拟实际盘点
            int actualStock = performPhysicalCount(product.getId());
            item.setActualStock(actualStock);
            item.setVariance(actualStock - product.getStock());
            
            inventoryItems.add(item);
            
            // 更新系统库存
            if (item.getVariance() != 0) {
                product.setStock(actualStock);
                productRepository.save(product);
            }
        }
        
        report.setItems(inventoryItems);
        report.setEndTime(LocalDateTime.now());
        
        return report;
    }
    
    /**
     * 配置管理更新 - 使用默认隔离级别
     * 对于配置类数据，使用数据库默认隔离级别即可
     */
    @Transactional
    public void updateSystemConfiguration(String key, String value) {
        SystemConfig config = systemConfigRepository.findByKey(key)
            .orElse(new SystemConfig());
        
        config.setKey(key);
        config.setValue(value);
        config.setUpdatedAt(LocalDateTime.now());
        
        systemConfigRepository.save(config);
        
        // 清除缓存
        cacheManager.evict("system-config", key);
    }
    
    private void createTransferLog(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        TransferLog log = new TransferLog();
        log.setFromAccountId(fromAccountId);
        log.setToAccountId(toAccountId);
        log.setAmount(amount);
        log.setTransferTime(LocalDateTime.now());
        
        transferLogRepository.save(log);
    }
    
    private int performPhysicalCount(Long productId) {
        // 模拟实际库存盘点
        return new Random().nextInt(100);
    }
}
```

## 📝 小结

Spring 事务管理是企业级应用的核心基础设施，它通过统一的抽象层支持多种事务技术，提供了灵活而强大的事务控制能力。

### 核心要点总结

- **架构设计** - 基于策略模式的可扩展架构，支持多种事务管理器
- **传播行为** - 7种传播行为满足不同的业务场景需求
- **隔离级别** - 4种隔离级别平衡一致性和性能
- **声明式事务** - 通过注解简化事务管理，提高开发效率
- **编程式事务** - 提供精确的事务控制能力

### 最佳实践指导

1. **合理选择传播行为** - 根据业务需求选择合适的传播策略
2. **适当设置隔离级别** - 平衡数据一致性和系统性能
3. **控制事务边界** - 保持事务尽可能小，避免长事务
4. **异常处理策略** - 正确配置回滚条件，确保数据一致性
5. **性能监控** - 建立事务性能监控体系，及时发现问题

通过深入理解 Spring 事务管理的原理和最佳实践，开发者可以构建出既高效又可靠的企业级应用系统。
