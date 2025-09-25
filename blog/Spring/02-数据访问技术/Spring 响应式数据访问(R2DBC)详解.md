---
title: Spring 响应式数据访问(R2DBC)详解
description: Spring 响应式数据访问(R2DBC)详解
tags: [Spring R2DBC, Reactive, 响应式编程]
category: Spring
date: 2025-09-25
---

# Spring 响应式数据访问(R2DBC)详解

## 🎯 概述

R2DBC（Reactive Relational Database Connectivity）是一种新的数据库连接规范，专门为响应式编程而设计。与传统的阻塞式 JDBC 不同，R2DBC 提供了完全非阻塞的数据库访问能力，能够充分利用现代硬件资源，实现高并发、低延迟的数据访问。Spring Data R2DBC 在 R2DBC 规范的基础上，提供了与 Spring Data 生态系统一致的编程模型，使开发者能够轻松构建响应式的数据访问层。

## 🏗️ R2DBC 核心架构

### 1. 响应式数据访问架构图

```
Spring R2DBC 响应式架构

┌─────────────────────────────────────────────────────────────┐
│                   应用层 (Application Layer)                 │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │  WebFlux        │  │  Reactive       │                   │
│  │  Controller     │  │  Service        │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                            │ Mono/Flux
┌─────────────────────────────────────────────────────────────┐
│                Spring Data R2DBC 层                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              R2dbcRepository                            │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │ReactiveCrud │ │ReactiveQuery│ │Custom       │       │ │
│  │  │Repository   │ │ByExample    │ Repository   │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              R2dbcEntityTemplate                        │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │Query Builder│ │Entity       │ │Criteria     │       │ │
│  │  │             │ │Operations   │ │Support      │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    R2DBC SPI 层                             │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              ConnectionFactory                          │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │Connection   │ │Statement    │ │Result       │       │ │
│  │  │Pool         │ │             │ │             │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │           Reactive Transaction Manager                  │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │Transaction  │ │Isolation    │ │Rollback     │       │ │
│  │  │Context      │ │Level        │ │Support      │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                  数据库驱动层 (Driver Layer)                  │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │   r2dbc-mysql   │ │ r2dbc-postgresql│ │   r2dbc-h2      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    数据库层 (Database Layer)                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │      MySQL      │ │   PostgreSQL    │ │       H2        │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 2. R2DBC 配置与连接管理

```java
/**
 * R2DBC 配置类
 * 配置响应式数据库连接和相关组件
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.repository")
@EnableR2dbcAuditing
public class R2dbcConfig extends AbstractR2dbcConfiguration {
    
    @Value("${spring.r2dbc.url}")
    private String url;
    
    @Value("${spring.r2dbc.username}")
    private String username;
    
    @Value("${spring.r2dbc.password}")
    private String password;
    
    @Value("${spring.r2dbc.pool.initial-size:10}")
    private int initialSize;
    
    @Value("${spring.r2dbc.pool.max-size:50}")
    private int maxSize;
    
    @Value("${spring.r2dbc.pool.max-idle-time:30m}")
    private Duration maxIdleTime;
    
    @Value("${spring.r2dbc.pool.validation-query:SELECT 1}")
    private String validationQuery;
    
    /**
     * 连接工厂配置
     */
    @Override
    @Bean
    public ConnectionFactory connectionFactory() {
        ConnectionFactoryOptions.Builder builder = ConnectionFactoryOptions.parse(url).mutate();
        
        if (username != null) {
            builder.option(USER, username);
        }
        if (password != null) {
            builder.option(PASSWORD, password);
        }
        
        ConnectionFactoryOptions options = builder.build();
        ConnectionFactory connectionFactory = ConnectionFactories.get(options);
        
        // 配置连接池
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder(connectionFactory)
            .initialSize(initialSize)
            .maxSize(maxSize)
            .maxIdleTime(maxIdleTime)
            .validationQuery(validationQuery)
            .name("reactive-pool")
            .registerJmx(true)
            .build();
        
        return new ConnectionPool(poolConfiguration);
    }
    
    /**
     * 响应式事务管理器
     */
    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
    
    /**
     * R2DBC 实体模板
     */
    @Bean
    public R2dbcEntityTemplate r2dbcEntityTemplate(DatabaseClient databaseClient) {
        return new R2dbcEntityTemplate(databaseClient);
    }
    
    /**
     * 数据库客户端定制
     */
    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .namedParameters(true)
            .build();
    }
    
    /**
     * 自定义转换器
     */
    @Override
    protected List<Object> getCustomConverters() {
        List<Object> converters = new ArrayList<>();
        converters.add(new JsonToMapConverter());
        converters.add(new MapToJsonConverter());
        converters.add(new LocalDateTimeToTimestampConverter());
        converters.add(new TimestampToLocalDateTimeConverter());
        return converters;
    }
    
    /**
     * 审计提供者
     */
    @Bean
    public ReactiveAuditorAware<String> auditorProvider() {
        return new ReactiveSpringSecurityAuditorAware();
    }
    
    /**
     * 连接池监控配置
     */
    @Bean
    public ConnectionPoolMetrics connectionPoolMetrics(ConnectionFactory connectionFactory) {
        if (connectionFactory instanceof ConnectionPool) {
            return ((ConnectionPool) connectionFactory).getMetrics();
        }
        return null;
    }
}

/**
 * 响应式审计提供者
 */
@Component
public class ReactiveSpringSecurityAuditorAware implements ReactiveAuditorAware<String> {
    
    @Override
    public Mono<String> getCurrentAuditor() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .cast(JwtAuthenticationToken.class)
            .map(JwtAuthenticationToken::getToken)
            .map(jwt -> jwt.getClaimAsString("preferred_username"))
            .switchIfEmpty(Mono.just("system"));
    }
}

/**
 * 自定义类型转换器
 */
@Component
@ReadingConverter
public class JsonToMapConverter implements Converter<String, Map<String, Object>> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Map<String, Object> convert(String source) {
        try {
            return objectMapper.readValue(source, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to parse JSON", e);
        }
    }
}

@Component
@WritingConverter
public class MapToJsonConverter implements Converter<Map<String, Object>, String> {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public String convert(Map<String, Object> source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize to JSON", e);
        }
    }
}
```

### 3. 响应式实体设计

```java
/**
 * 响应式基础实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ReactiveBaseEntity {
    
    @Id
    private Long id;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    private String createdBy;
    
    @LastModifiedBy
    private String updatedBy;
    
    @Version
    private Long version;
    
    private Boolean deleted = false;
    
    private LocalDateTime deletedAt;
    
    private String deletedBy;
    
    /**
     * 检查是否为新实体
     */
    public boolean isNew() {
        return this.id == null;
    }
    
    /**
     * 软删除
     */
    public void softDelete(String deletedBy) {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletedBy = deletedBy;
    }
    
    /**
     * 恢复软删除
     */
    public void restore() {
        this.deleted = false;
        this.deletedAt = null;
        this.deletedBy = null;
    }
}

/**
 * 用户实体 - 响应式版本
 */
@Table("users")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReactiveUser extends ReactiveBaseEntity {
    
    @Column("username")
    private String username;
    
    @Column("email")
    private String email;
    
    @Column("password")
    private String password;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("age")
    private Integer age;
    
    @Column("status")
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column("gender")
    private Gender gender;
    
    @Column("phone")
    private String phone;
    
    @Column("avatar_url")
    private String avatarUrl;
    
    @Column("department_id")
    private Long departmentId;
    
    @Column("last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column("login_count")
    private Long loginCount = 0L;
    
    // JSON 字段映射
    @Column("preferences")
    private Map<String, Object> preferences = new HashMap<>();
    
    @Column("metadata")
    private Map<String, Object> metadata = new HashMap<>();
    
    // 业务方法
    
    public ReactiveUser(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return username;
        }
    }
    
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !getDeleted();
    }
    
    public void updateLoginInfo() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
    }
    
    public void setPreference(String key, Object value) {
        if (preferences == null) {
            preferences = new HashMap<>();
        }
        preferences.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getPreference(String key, Class<T> type) {
        if (preferences == null) {
            return null;
        }
        Object value = preferences.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
}

/**
 * 订单实体 - 响应式版本
 */
@Table("orders")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ReactiveOrder extends ReactiveBaseEntity {
    
    @Column("order_number")
    private String orderNumber;
    
    @Column("user_id")
    private Long userId;
    
    @Column("product_id")
    private Long productId;
    
    @Column("quantity")
    private Integer quantity;
    
    @Column("unit_price")
    private BigDecimal unitPrice;
    
    @Column("total_amount")
    private BigDecimal totalAmount;
    
    @Column("status")
    private OrderStatus status = OrderStatus.PENDING;
    
    @Column("order_date")
    private LocalDateTime orderDate;
    
    @Column("delivery_address")
    private String deliveryAddress;
    
    @Column("notes")
    private String notes;
    
    public ReactiveOrder(String orderNumber, Long userId, Long productId, Integer quantity, BigDecimal unitPrice) {
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.orderDate = LocalDateTime.now();
    }
    
    public void calculateTotalAmount() {
        if (unitPrice != null && quantity != null) {
            this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING || status == OrderStatus.CONFIRMED;
    }
    
    public void confirm() {
        if (status == OrderStatus.PENDING) {
            this.status = OrderStatus.CONFIRMED;
        } else {
            throw new IllegalStateException("订单状态不允许确认");
        }
    }
    
    public void cancel() {
        if (canBeCancelled()) {
            this.status = OrderStatus.CANCELLED;
        } else {
            throw new IllegalStateException("订单状态不允许取消");
        }
    }
}

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING("待处理"),
    CONFIRMED("已确认"),
    PROCESSING("处理中"),
    SHIPPED("已发货"),
    DELIVERED("已送达"),
    CANCELLED("已取消"),
    REFUNDED("已退款");
    
    private final String description;
    
    OrderStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## 🔄 响应式 Repository 实现

### 1. 基础响应式 Repository

```java
/**
 * 响应式基础 Repository 接口
 */
@NoRepositoryBean
public interface ReactiveBaseRepository<T, ID> extends R2dbcRepository<T, ID> {
    
    /**
     * 根据 ID 查找未删除的实体
     */
    @Query("SELECT * FROM #{#tableName} WHERE id = :id AND deleted = false")
    Mono<T> findByIdAndNotDeleted(@Param("id") ID id);
    
    /**
     * 查找所有未删除的实体
     */
    @Query("SELECT * FROM #{#tableName} WHERE deleted = false ORDER BY created_at DESC")
    Flux<T> findAllNotDeleted();
    
    /**
     * 分页查找所有未删除的实体
     */
    @Query("SELECT * FROM #{#tableName} WHERE deleted = false ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<T> findAllNotDeleted(@Param("size") int size, @Param("offset") long offset);
    
    /**
     * 统计未删除的实体数量
     */
    @Query("SELECT COUNT(*) FROM #{#tableName} WHERE deleted = false")
    Mono<Long> countNotDeleted();
    
    /**
     * 软删除
     */
    @Modifying
    @Query("UPDATE #{#tableName} SET deleted = true, deleted_at = :deletedAt, deleted_by = :deletedBy WHERE id = :id")
    Mono<Integer> softDeleteById(@Param("id") ID id, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
    
    /**
     * 批量软删除
     */
    @Modifying
    @Query("UPDATE #{#tableName} SET deleted = true, deleted_at = :deletedAt, deleted_by = :deletedBy WHERE id IN (:ids)")
    Mono<Integer> softDeleteByIds(@Param("ids") Collection<ID> ids, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
    
    /**
     * 恢复软删除
     */
    @Modifying
    @Query("UPDATE #{#tableName} SET deleted = false, deleted_at = null, deleted_by = null WHERE id = :id")
    Mono<Integer> restoreSoftDeleted(@Param("id") ID id);
    
    /**
     * 检查实体是否存在且未删除
     */
    @Query("SELECT COUNT(*) > 0 FROM #{#tableName} WHERE id = :id AND deleted = false")
    Mono<Boolean> existsByIdAndNotDeleted(@Param("id") ID id);
}

/**
 * 用户响应式 Repository
 */
public interface ReactiveUserRepository extends ReactiveBaseRepository<ReactiveUser, Long>, ReactiveUserRepositoryCustom {
    
    // 基础查询方法
    
    Mono<ReactiveUser> findByUsernameAndDeletedFalse(String username);
    
    Mono<ReactiveUser> findByEmailAndDeletedFalse(String email);
    
    Flux<ReactiveUser> findByStatusAndDeletedFalse(UserStatus status);
    
    Flux<ReactiveUser> findByDepartmentIdAndDeletedFalse(Long departmentId);
    
    Flux<ReactiveUser> findByStatusAndDeletedFalseOrderByCreatedAtDesc(UserStatus status);
    
    // 复杂查询
    
    @Query("SELECT * FROM users WHERE deleted = false AND status = :status AND department_id = :departmentId")
    Flux<ReactiveUser> findByStatusAndDepartment(@Param("status") UserStatus status, @Param("departmentId") Long departmentId);
    
    @Query("SELECT * FROM users WHERE deleted = false AND age BETWEEN :minAge AND :maxAge ORDER BY age")
    Flux<ReactiveUser> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    @Query("SELECT * FROM users WHERE deleted = false AND last_login_at < :date")
    Flux<ReactiveUser> findInactiveUsersSince(@Param("date") LocalDateTime date);
    
    @Query("SELECT * FROM users WHERE deleted = false AND (username LIKE :keyword OR email LIKE :keyword OR first_name LIKE :keyword OR last_name LIKE :keyword)")
    Flux<ReactiveUser> searchByKeyword(@Param("keyword") String keyword);
    
    // 统计查询
    
    @Query("SELECT COUNT(*) FROM users WHERE deleted = false AND status = :status")
    Mono<Long> countByStatus(@Param("status") UserStatus status);
    
    @Query("SELECT status, COUNT(*) as count FROM users WHERE deleted = false GROUP BY status")
    Flux<UserStatusCount> getStatusStatistics();
    
    @Query("SELECT department_id, COUNT(*) as count FROM users WHERE deleted = false GROUP BY department_id")
    Flux<DepartmentUserCount> getDepartmentStatistics();
    
    // 更新操作
    
    @Modifying
    @Query("UPDATE users SET status = :status, updated_at = :updatedAt WHERE id IN (:ids)")
    Mono<Integer> updateStatusByIds(@Param("ids") Collection<Long> ids, @Param("status") UserStatus status, @Param("updatedAt") LocalDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE users SET last_login_at = :loginTime, login_count = login_count + 1, updated_at = :updatedAt WHERE id = :id")
    Mono<Integer> updateLoginInfo(@Param("id") Long id, @Param("loginTime") LocalDateTime loginTime, @Param("updatedAt") LocalDateTime updatedAt);
    
    @Modifying
    @Query("UPDATE users SET password = :password, updated_at = :updatedAt WHERE id = :id")
    Mono<Integer> updatePassword(@Param("id") Long id, @Param("password") String password, @Param("updatedAt") LocalDateTime updatedAt);
    
    // 分页查询
    
    @Query("SELECT * FROM users WHERE deleted = false AND status = :status ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<ReactiveUser> findByStatusWithPaging(@Param("status") UserStatus status, @Param("size") int size, @Param("offset") long offset);
    
    @Query("SELECT COUNT(*) FROM users WHERE deleted = false AND status = :status")
    Mono<Long> countByStatusForPaging(@Param("status") UserStatus status);
    
    // 删除操作
    
    @Modifying
    @Query("DELETE FROM users WHERE deleted = true AND deleted_at < :cutoffDate")
    Mono<Integer> permanentlyDeleteOldSoftDeletedUsers(@Param("cutoffDate") LocalDateTime cutoffDate);
}

/**
 * 用户统计 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusCount {
    private UserStatus status;
    private Long count;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUserCount {
    private Long departmentId;
    private Long count;
}
```

### 2. 自定义响应式 Repository 实现

```java
/**
 * 自定义响应式 Repository 接口
 */
public interface ReactiveUserRepositoryCustom {
    
    /**
     * 动态条件查询
     */
    Flux<ReactiveUser> findUsersWithCriteria(UserSearchCriteria criteria);
    
    /**
     * 分页动态条件查询
     */
    Mono<ReactivePageResult<ReactiveUser>> findUsersWithCriteriaAndPaging(UserSearchCriteria criteria, ReactivePageRequest pageRequest);
    
    /**
     * 复杂统计查询
     */
    Flux<UserStatisticsDTO> getUserStatistics(UserStatisticsQuery query);
    
    /**
     * 批量更新用户信息
     */
    Mono<Integer> batchUpdateUsers(List<UserUpdateDTO> updates);
    
    /**
     * 获取用户活跃度报告
     */
    Flux<UserActivityReport> getUserActivityReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * 复杂的用户搜索
     */
    Flux<ReactiveUser> searchUsers(UserSearchQuery query);
    
    /**
     * 流式处理大量用户数据
     */
    Flux<ReactiveUser> streamAllUsers();
    
    /**
     * 用户数据导出
     */
    Flux<UserExportDTO> exportUsers(UserExportCriteria criteria);
}

/**
 * 自定义响应式 Repository 实现
 */
@Repository
public class ReactiveUserRepositoryImpl implements ReactiveUserRepositoryCustom {
    
    private final R2dbcEntityTemplate entityTemplate;
    private final DatabaseClient databaseClient;
    
    public ReactiveUserRepositoryImpl(R2dbcEntityTemplate entityTemplate, DatabaseClient databaseClient) {
        this.entityTemplate = entityTemplate;
        this.databaseClient = databaseClient;
    }
    
    @Override
    public Flux<ReactiveUser> findUsersWithCriteria(UserSearchCriteria criteria) {
        return buildCriteriaQuery(criteria)
            .flatMapMany(sql -> databaseClient.sql(sql.getSql())
                .bind(sql.getParameters())
                .map((row, metadata) -> mapRowToUser(row))
                .all());
    }
    
    @Override
    public Mono<ReactivePageResult<ReactiveUser>> findUsersWithCriteriaAndPaging(UserSearchCriteria criteria, ReactivePageRequest pageRequest) {
        return buildCriteriaQuery(criteria)
            .flatMap(sql -> {
                // 获取总数
                Mono<Long> totalMono = databaseClient.sql(sql.getCountSql())
                    .bind(sql.getParameters())
                    .map((row, metadata) -> row.get(0, Long.class))
                    .one();
                
                // 获取分页数据
                String pagedSql = sql.getSql() + " LIMIT :limit OFFSET :offset";
                Flux<ReactiveUser> dataFlux = databaseClient.sql(pagedSql)
                    .bind(sql.getParameters())
                    .bind("limit", pageRequest.getSize())
                    .bind("offset", pageRequest.getOffset())
                    .map((row, metadata) -> mapRowToUser(row))
                    .all();
                
                return Mono.zip(totalMono, dataFlux.collectList())
                    .map(tuple -> new ReactivePageResult<>(
                        tuple.getT2(), 
                        pageRequest, 
                        tuple.getT1()
                    ));
            });
    }
    
    @Override
    public Flux<UserStatisticsDTO> getUserStatistics(UserStatisticsQuery query) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                COALESCE(d.name, '未分配') as department_name,
                COUNT(u.id) as user_count,
                AVG(u.age) as avg_age,
                MIN(u.age) as min_age,
                MAX(u.age) as max_age,
                COUNT(CASE WHEN u.status = 'ACTIVE' THEN 1 END) as active_count,
                COUNT(CASE WHEN u.last_login_at > :sinceDate THEN 1 END) as recent_login_count
            FROM users u
            LEFT JOIN departments d ON u.department_id = d.id
            WHERE u.deleted = false
            """);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("sinceDate", LocalDateTime.now().minusDays(30));
        
        if (query.getDepartmentIds() != null && !query.getDepartmentIds().isEmpty()) {
            sql.append(" AND u.department_id IN (:departmentIds)");
            parameters.put("departmentIds", query.getDepartmentIds());
        }
        
        if (query.getStatus() != null) {
            sql.append(" AND u.status = :status");
            parameters.put("status", query.getStatus().name());
        }
        
        sql.append(" GROUP BY d.name");
        
        if (query.getMinUserCount() != null) {
            sql.append(" HAVING COUNT(u.id) >= :minUserCount");
            parameters.put("minUserCount", query.getMinUserCount());
        }
        
        sql.append(" ORDER BY user_count DESC");
        
        return bindParameters(databaseClient.sql(sql.toString()), parameters)
            .map((row, metadata) -> UserStatisticsDTO.builder()
                .departmentName(row.get("department_name", String.class))
                .userCount(row.get("user_count", Long.class))
                .avgAge(row.get("avg_age", Double.class))
                .minAge(row.get("min_age", Integer.class))
                .maxAge(row.get("max_age", Integer.class))
                .activeCount(row.get("active_count", Long.class))
                .recentLoginCount(row.get("recent_login_count", Long.class))
                .build())
            .all();
    }
    
    @Override
    @Transactional
    public Mono<Integer> batchUpdateUsers(List<UserUpdateDTO> updates) {
        if (updates.isEmpty()) {
            return Mono.just(0);
        }
        
        return Flux.fromIterable(updates)
            .flatMap(update -> {
                Query query = Query.query(Criteria.where("id").is(update.getId()));
                Update updateObj = Update.update("updated_at", LocalDateTime.now());
                
                if (update.getFirstName() != null) {
                    updateObj = updateObj.set("first_name", update.getFirstName());
                }
                if (update.getLastName() != null) {
                    updateObj = updateObj.set("last_name", update.getLastName());
                }
                if (update.getEmail() != null) {
                    updateObj = updateObj.set("email", update.getEmail());
                }
                if (update.getStatus() != null) {
                    updateObj = updateObj.set("status", update.getStatus());
                }
                if (update.getDepartmentId() != null) {
                    updateObj = updateObj.set("department_id", update.getDepartmentId());
                }
                
                return entityTemplate.update(query, updateObj, ReactiveUser.class);
            })
            .reduce(0, Integer::sum);
    }
    
    @Override
    public Flux<UserActivityReport> getUserActivityReport(LocalDate startDate, LocalDate endDate) {
        String sql = """
            SELECT 
                u.id,
                u.username,
                u.email,
                u.department_id,
                d.name as department_name,
                u.login_count,
                u.last_login_at,
                COUNT(o.id) as order_count,
                COALESCE(SUM(o.total_amount), 0) as total_amount,
                CASE 
                    WHEN u.last_login_at > DATE_SUB(NOW(), INTERVAL 7 DAY) THEN 'ACTIVE'
                    WHEN u.last_login_at > DATE_SUB(NOW(), INTERVAL 30 DAY) THEN 'MODERATE'
                    ELSE 'INACTIVE'
                END as activity_level
            FROM users u
            LEFT JOIN departments d ON u.department_id = d.id
            LEFT JOIN orders o ON u.id = o.user_id 
                AND o.created_at BETWEEN :startDate AND :endDate
            WHERE u.deleted = false
            GROUP BY u.id, u.username, u.email, u.department_id, d.name, u.login_count, u.last_login_at
            ORDER BY u.last_login_at DESC
            """;
        
        return databaseClient.sql(sql)
            .bind("startDate", startDate)
            .bind("endDate", endDate.plusDays(1))
            .map((row, metadata) -> UserActivityReport.builder()
                .userId(row.get("id", Long.class))
                .username(row.get("username", String.class))
                .email(row.get("email", String.class))
                .departmentId(row.get("department_id", Long.class))
                .departmentName(row.get("department_name", String.class))
                .loginCount(row.get("login_count", Long.class))
                .lastLoginAt(row.get("last_login_at", LocalDateTime.class))
                .orderCount(row.get("order_count", Integer.class))
                .totalAmount(row.get("total_amount", BigDecimal.class))
                .activityLevel(ActivityLevel.valueOf(row.get("activity_level", String.class)))
                .build())
            .all();
    }
    
    @Override
    public Flux<ReactiveUser> searchUsers(UserSearchQuery query) {
        return buildSearchQuery(query)
            .flatMapMany(sql -> databaseClient.sql(sql.getSql())
                .bind(sql.getParameters())
                .map((row, metadata) -> mapRowToUser(row))
                .all());
    }
    
    @Override
    public Flux<ReactiveUser> streamAllUsers() {
        return entityTemplate.select(ReactiveUser.class)
            .matching(Query.query(Criteria.where("deleted").is(false)))
            .all()
            .delayElements(Duration.ofMillis(10)); // 控制流速，避免背压
    }
    
    @Override
    public Flux<UserExportDTO> exportUsers(UserExportCriteria criteria) {
        return buildExportQuery(criteria)
            .flatMapMany(sql -> databaseClient.sql(sql.getSql())
                .bind(sql.getParameters())
                .map((row, metadata) -> UserExportDTO.builder()
                    .username(row.get("username", String.class))
                    .email(row.get("email", String.class))
                    .firstName(row.get("first_name", String.class))
                    .lastName(row.get("last_name", String.class))
                    .age(row.get("age", Integer.class))
                    .status(UserStatus.valueOf(row.get("status", String.class)))
                    .departmentName(row.get("department_name", String.class))
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .lastLoginAt(row.get("last_login_at", LocalDateTime.class))
                    .build())
                .all())
            .buffer(1000) // 分批处理，避免内存溢出
            .flatMap(Flux::fromIterable);
    }
    
    // 辅助方法
    
    private Mono<SqlAndParameters> buildCriteriaQuery(UserSearchCriteria criteria) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE deleted = false");
        StringBuilder countSql = new StringBuilder("SELECT COUNT(*) FROM users WHERE deleted = false");
        Map<String, Object> parameters = new HashMap<>();
        
        if (criteria.getUsername() != null && !criteria.getUsername().trim().isEmpty()) {
            sql.append(" AND username LIKE :username");
            countSql.append(" AND username LIKE :username");
            parameters.put("username", "%" + criteria.getUsername() + "%");
        }
        
        if (criteria.getEmail() != null && !criteria.getEmail().trim().isEmpty()) {
            sql.append(" AND email LIKE :email");
            countSql.append(" AND email LIKE :email");
            parameters.put("email", "%" + criteria.getEmail() + "%");
        }
        
        if (criteria.getStatus() != null) {
            sql.append(" AND status = :status");
            countSql.append(" AND status = :status");
            parameters.put("status", criteria.getStatus().name());
        }
        
        if (criteria.getDepartmentId() != null) {
            sql.append(" AND department_id = :departmentId");
            countSql.append(" AND department_id = :departmentId");
            parameters.put("departmentId", criteria.getDepartmentId());
        }
        
        if (criteria.getMinAge() != null) {
            sql.append(" AND age >= :minAge");
            countSql.append(" AND age >= :minAge");
            parameters.put("minAge", criteria.getMinAge());
        }
        
        if (criteria.getMaxAge() != null) {
            sql.append(" AND age <= :maxAge");
            countSql.append(" AND age <= :maxAge");
            parameters.put("maxAge", criteria.getMaxAge());
        }
        
        if (criteria.getCreatedAfter() != null) {
            sql.append(" AND created_at >= :createdAfter");
            countSql.append(" AND created_at >= :createdAfter");
            parameters.put("createdAfter", criteria.getCreatedAfter());
        }
        
        if (criteria.getCreatedBefore() != null) {
            sql.append(" AND created_at <= :createdBefore");
            countSql.append(" AND created_at <= :createdBefore");
            parameters.put("createdBefore", criteria.getCreatedBefore());
        }
        
        sql.append(" ORDER BY created_at DESC");
        
        return Mono.just(new SqlAndParameters(sql.toString(), countSql.toString(), parameters));
    }
    
    private Mono<SqlAndParameters> buildSearchQuery(UserSearchQuery query) {
        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE deleted = false");
        Map<String, Object> parameters = new HashMap<>();
        
        if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
            sql.append(" AND (username LIKE :keyword OR email LIKE :keyword OR first_name LIKE :keyword OR last_name LIKE :keyword)");
            parameters.put("keyword", "%" + query.getKeyword() + "%");
        }
        
        if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
            sql.append(" AND status IN (:statuses)");
            parameters.put("statuses", query.getStatuses().stream().map(Enum::name).collect(Collectors.toList()));
        }
        
        if (query.getDepartmentIds() != null && !query.getDepartmentIds().isEmpty()) {
            sql.append(" AND department_id IN (:departmentIds)");
            parameters.put("departmentIds", query.getDepartmentIds());
        }
        
        sql.append(" ORDER BY last_login_at DESC");
        
        return Mono.just(new SqlAndParameters(sql.toString(), null, parameters));
    }
    
    private Mono<SqlAndParameters> buildExportQuery(UserExportCriteria criteria) {
        StringBuilder sql = new StringBuilder("""
            SELECT u.*, d.name as department_name 
            FROM users u 
            LEFT JOIN departments d ON u.department_id = d.id 
            WHERE u.deleted = false
            """);
        
        Map<String, Object> parameters = new HashMap<>();
        
        if (criteria.getStatuses() != null && !criteria.getStatuses().isEmpty()) {
            sql.append(" AND u.status IN (:statuses)");
            parameters.put("statuses", criteria.getStatuses().stream().map(Enum::name).collect(Collectors.toList()));
        }
        
        if (criteria.getDepartmentIds() != null && !criteria.getDepartmentIds().isEmpty()) {
            sql.append(" AND u.department_id IN (:departmentIds)");
            parameters.put("departmentIds", criteria.getDepartmentIds());
        }
        
        if (criteria.getCreatedAfter() != null) {
            sql.append(" AND u.created_at >= :createdAfter");
            parameters.put("createdAfter", criteria.getCreatedAfter());
        }
        
        if (criteria.getCreatedBefore() != null) {
            sql.append(" AND u.created_at <= :createdBefore");
            parameters.put("createdBefore", criteria.getCreatedBefore());
        }
        
        sql.append(" ORDER BY u.created_at DESC");
        
        return Mono.just(new SqlAndParameters(sql.toString(), null, parameters));
    }
    
    private ReactiveUser mapRowToUser(Row row) {
        ReactiveUser user = new ReactiveUser();
        user.setId(row.get("id", Long.class));
        user.setUsername(row.get("username", String.class));
        user.setEmail(row.get("email", String.class));
        user.setPassword(row.get("password", String.class));
        user.setFirstName(row.get("first_name", String.class));
        user.setLastName(row.get("last_name", String.class));
        user.setAge(row.get("age", Integer.class));
        
        String statusStr = row.get("status", String.class);
        if (statusStr != null) {
            user.setStatus(UserStatus.valueOf(statusStr));
        }
        
        String genderStr = row.get("gender", String.class);
        if (genderStr != null) {
            user.setGender(Gender.valueOf(genderStr));
        }
        
        user.setPhone(row.get("phone", String.class));
        user.setAvatarUrl(row.get("avatar_url", String.class));
        user.setDepartmentId(row.get("department_id", Long.class));
        user.setLastLoginAt(row.get("last_login_at", LocalDateTime.class));
        user.setLoginCount(row.get("login_count", Long.class));
        user.setCreatedAt(row.get("created_at", LocalDateTime.class));
        user.setUpdatedAt(row.get("updated_at", LocalDateTime.class));
        user.setCreatedBy(row.get("created_by", String.class));
        user.setUpdatedBy(row.get("updated_by", String.class));
        user.setVersion(row.get("version", Long.class));
        user.setDeleted(row.get("deleted", Boolean.class));
        user.setDeletedAt(row.get("deleted_at", LocalDateTime.class));
        user.setDeletedBy(row.get("deleted_by", String.class));
        
        // JSON 字段处理
        String preferencesJson = row.get("preferences", String.class);
        if (preferencesJson != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                user.setPreferences(mapper.readValue(preferencesJson, new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                user.setPreferences(new HashMap<>());
            }
        }
        
        String metadataJson = row.get("metadata", String.class);
        if (metadataJson != null) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                user.setMetadata(mapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {}));
            } catch (JsonProcessingException e) {
                user.setMetadata(new HashMap<>());
            }
        }
        
        return user;
    }
    
    private DatabaseClient.GenericExecuteSpec bindParameters(DatabaseClient.GenericExecuteSpec spec, Map<String, Object> parameters) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            spec = spec.bind(entry.getKey(), entry.getValue());
        }
        return spec;
    }
    
    /**
     * SQL 和参数包装类
     */
    @Data
    @AllArgsConstructor
    private static class SqlAndParameters {
        private String sql;
        private String countSql;
        private Map<String, Object> parameters;
    }
}

/**
 * 响应式分页结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactivePageResult<T> {
    private List<T> content;
    private ReactivePageRequest pageRequest;
    private long totalElements;
    
    public int getTotalPages() {
        return (int) Math.ceil((double) totalElements / pageRequest.getSize());
    }
    
    public boolean hasNext() {
        return pageRequest.getPageNumber() < getTotalPages() - 1;
    }
    
    public boolean hasPrevious() {
        return pageRequest.getPageNumber() > 0;
    }
    
    public boolean isFirst() {
        return pageRequest.getPageNumber() == 0;
    }
    
    public boolean isLast() {
        return pageRequest.getPageNumber() == getTotalPages() - 1;
    }
}

/**
 * 响应式分页请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReactivePageRequest {
    private int pageNumber;
    private int size;
    private List<Sort> sorts = new ArrayList<>();
    
    public ReactivePageRequest(int pageNumber, int size) {
        this.pageNumber = pageNumber;
        this.size = size;
    }
    
    public long getOffset() {
        return (long) pageNumber * size;
    }
    
    public ReactivePageRequest withSort(String property, Sort.Direction direction) {
        sorts.add(new Sort(property, direction));
        return this;
    }
    
    @Data
    @AllArgsConstructor
    public static class Sort {
        private String property;
        private Direction direction;
        
        public enum Direction {
            ASC, DESC
        }
    }
}
```

## 📝 小结

Spring 响应式数据访问(R2DBC)为现代高并发应用提供了全新的数据访问解决方案，通过非阻塞 I/O 和响应式编程模型，能够显著提升应用的并发处理能力和资源利用效率。

### 核心特性总结

- **非阻塞 I/O** - 完全异步的数据库访问，提升并发性能
- **响应式编程** - 基于 Mono/Flux 的响应式数据流处理
- **背压支持** - 智能的流量控制和资源管理
- **连接池优化** - 高效的连接池管理和监控
- **事务支持** - 响应式事务管理和传播行为

### 最佳实践要点

1. **合理使用背压** - 避免内存溢出和系统过载
2. **连接池配置** - 根据负载合理配置连接池参数
3. **错误处理** - 完善的异常处理和重试机制
4. **性能监控** - 建立响应式应用的性能监控体系
5. **资源管理** - 确保响应式流的正确关闭和资源释放

通过深入掌握 Spring R2DBC 的核心原理和实践技巧，开发者可以构建出高性能、高并发的响应式数据访问层，为现代云原生应用提供强大的数据支撑能力。
