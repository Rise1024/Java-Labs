---
title: Spring Data JPA 企业级实战
description: Spring Data JPA 企业级实战
tags: [Spring Data JPA, JPA, Hibernate]
category: Spring
date: 2025-09-25
---

# Spring Data JPA 企业级实战

## 🎯 概述

Spring Data JPA 是 Spring 生态系统中最受欢迎的数据访问解决方案之一，它在 JPA（Java Persistence API）的基础上提供了更高层次的抽象，大大简化了数据访问层的开发。Spring Data JPA 不仅支持标准的 CRUD 操作，还提供了强大的查询能力、自动分页、审计功能、缓存集成等企业级特性。本文将深入探讨 Spring Data JPA 在复杂企业环境中的应用实战，包括高级映射、性能优化、复杂查询构建等核心技术。

## 🏗️ Spring Data JPA 企业级架构

### 1. 架构层次图

```
Spring Data JPA 企业级架构

┌─────────────────────────────────────────────────────────────┐
│                    展示层 (Presentation Layer)               │
│  ┌─────────────────┐  ┌─────────────────┐                   │
│  │   Controller    │  │   REST API      │                   │
│  │                 │  │                 │                   │
│  └─────────────────┘  └─────────────────┘                   │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    服务层 (Service Layer)                    │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │               Business Services                         │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │Domain       │ │Application  │ │Integration  │       │ │
│  │  │Service      │ │Service      │ │Service      │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                 数据访问层 (Data Access Layer)                │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Spring Data JPA                            │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │JpaRepository│ │Custom       │ │Specification│       │ │
│  │  │             │ │Repository   │ │Executor     │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │               JPA Implementation                        │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │EntityManager│ │Criteria API │ │JPQL         │       │ │
│  │  │             │ │             │ │             │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │                  Hibernate                              │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐       │ │
│  │  │Second Level │ │Query Cache  │ │Connection   │       │ │
│  │  │Cache        │ │             │ │Pool         │       │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘       │ │
│  └─────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                            │
┌─────────────────────────────────────────────────────────────┐
│                    数据库层 (Database Layer)                 │
│  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐ │
│  │      MySQL      │ │   PostgreSQL    │ │     Oracle      │ │
│  └─────────────────┘ └─────────────────┘ └─────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### 2. 企业级实体设计模式

#### 基础实体抽象类

```java
/**
 * 基础实体抽象类
 * 包含企业级应用中常见的审计字段和通用方法
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
    
    @Version
    @Column(name = "version")
    private Long version;
    
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Column(name = "deleted_by")
    private String deletedBy;
    
    // 构造函数
    protected BaseEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    public Boolean getDeleted() {
        return deleted;
    }
    
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
    
    public String getDeletedBy() {
        return deletedBy;
    }
    
    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
    
    /**
     * 软删除方法
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
    
    /**
     * 检查是否为新实体
     */
    public boolean isNew() {
        return this.id == null;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BaseEntity that = (BaseEntity) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
               "id=" + id +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               ", version=" + version +
               ", deleted=" + deleted +
               '}';
    }
}

/**
 * 审计配置
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
}

/**
 * Spring Security 审计提供者
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() ||
            authentication instanceof AnonymousAuthenticationToken) {
            return Optional.of("system");
        }
        
        return Optional.of(authentication.getName());
    }
}
```

#### 复杂实体关系映射

```java
/**
 * 用户实体 - 展示复杂实体映射
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email"),
    @Index(name = "idx_department_id", columnList = "department_id"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@NamedQueries({
    @NamedQuery(
        name = "User.findActiveUsersByDepartment",
        query = "SELECT u FROM User u WHERE u.department.id = :departmentId AND u.status = :status AND u.deleted = false"
    ),
    @NamedQuery(
        name = "User.findUserStatistics",
        query = "SELECT new com.example.dto.UserStatisticsDTO(u.department.name, COUNT(u), AVG(u.age)) " +
                "FROM User u WHERE u.deleted = false GROUP BY u.department.name"
    )
})
@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "User.withDepartment",
        attributeNodes = @NamedAttributeNode("department")
    ),
    @NamedEntityGraph(
        name = "User.withDepartmentAndRoles",
        attributeNodes = {
            @NamedAttributeNode("department"),
            @NamedAttributeNode("roles")
        }
    ),
    @NamedEntityGraph(
        name = "User.complete",
        attributeNodes = {
            @NamedAttributeNode("department"),
            @NamedAttributeNode("roles"),
            @NamedAttributeNode(value = "profile", subgraph = "profile.addresses")
        },
        subgraphs = {
            @NamedSubgraph(
                name = "profile.addresses",
                attributeNodes = @NamedAttributeNode("addresses")
            )
        }
    )
})
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity {
    
    @Column(name = "username", unique = true, nullable = false, length = 50)
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    @Email(message = "邮箱格式不正确")
    @Size(max = 100)
    private String email;
    
    @Column(name = "password", nullable = false)
    @Size(min = 6, message = "密码长度不能少于6位")
    private String password;
    
    @Column(name = "first_name", length = 50)
    @Size(max = 50)
    private String firstName;
    
    @Column(name = "last_name", length = 50)
    @Size(max = 50)
    private String lastName;
    
    @Column(name = "age")
    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄不能超过150")
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;
    
    @Column(name = "phone", length = 20)
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "电话号码格式不正确")
    private String phone;
    
    @Column(name = "avatar_url")
    private String avatarUrl;
    
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;
    
    @Column(name = "login_count")
    private Long loginCount = 0L;
    
    // 多对一关系 - 部门
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "fk_user_department"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Department department;
    
    // 多对多关系 - 角色
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_user_roles_user")),
        inverseJoinColumns = @JoinColumn(name = "role_id", foreignKey = @ForeignKey(name = "fk_user_roles_role")),
        indexes = {
            @Index(name = "idx_user_roles_user", columnList = "user_id"),
            @Index(name = "idx_user_roles_role", columnList = "role_id")
        }
    )
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<Role> roles = new HashSet<>();
    
    // 一对一关系 - 用户档案
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @PrimaryKeyJoinColumn
    private UserProfile profile;
    
    // 一对多关系 - 用户订单
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Order> orders = new ArrayList<>();
    
    // 一对多关系 - 用户地址
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("isDefault DESC, createdAt DESC")
    private List<Address> addresses = new ArrayList<>();
    
    // JSON 属性存储
    @Type(JsonBinaryType.class)
    @Column(name = "preferences", columnDefinition = "json")
    private Map<String, Object> preferences = new HashMap<>();
    
    @Type(JsonBinaryType.class)
    @Column(name = "metadata", columnDefinition = "json")
    private Map<String, Object> metadata = new HashMap<>();
    
    // 构造函数
    public User() {
        super();
    }
    
    public User(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // 业务方法
    
    /**
     * 添加角色
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }
    
    /**
     * 移除角色
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }
    
    /**
     * 清空所有角色
     */
    public void clearRoles() {
        for (Role role : new HashSet<>(this.roles)) {
            removeRole(role);
        }
    }
    
    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String roleName) {
        return roles.stream().anyMatch(role -> role.getName().equals(roleName));
    }
    
    /**
     * 检查是否有任意指定角色
     */
    public boolean hasAnyRole(String... roleNames) {
        Set<String> roleNameSet = Set.of(roleNames);
        return roles.stream().anyMatch(role -> roleNameSet.contains(role.getName()));
    }
    
    /**
     * 获取角色名称列表
     */
    public List<String> getRoleNames() {
        return roles.stream()
                   .map(Role::getName)
                   .sorted()
                   .collect(Collectors.toList());
    }
    
    /**
     * 添加订单
     */
    public void addOrder(Order order) {
        orders.add(order);
        order.setUser(this);
    }
    
    /**
     * 移除订单
     */
    public void removeOrder(Order order) {
        orders.remove(order);
        order.setUser(null);
    }
    
    /**
     * 添加地址
     */
    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }
    
    /**
     * 移除地址
     */
    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }
    
    /**
     * 获取默认地址
     */
    public Optional<Address> getDefaultAddress() {
        return addresses.stream()
                       .filter(Address::getIsDefault)
                       .findFirst();
    }
    
    /**
     * 设置默认地址
     */
    public void setDefaultAddress(Address newDefaultAddress) {
        // 清除当前默认地址
        addresses.forEach(address -> address.setIsDefault(false));
        
        // 设置新的默认地址
        if (newDefaultAddress != null && addresses.contains(newDefaultAddress)) {
            newDefaultAddress.setIsDefault(true);
        }
    }
    
    /**
     * 更新登录信息
     */
    public void updateLoginInfo() {
        this.lastLoginAt = LocalDateTime.now();
        this.loginCount = (this.loginCount == null ? 0 : this.loginCount) + 1;
    }
    
    /**
     * 获取全名
     */
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
    
    /**
     * 检查是否激活
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE && !getDeleted();
    }
    
    /**
     * 激活用户
     */
    public void activate() {
        this.status = UserStatus.ACTIVE;
    }
    
    /**
     * 停用用户
     */
    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }
    
    /**
     * 锁定用户
     */
    public void lock() {
        this.status = UserStatus.LOCKED;
    }
    
    /**
     * 设置偏好设置
     */
    public void setPreference(String key, Object value) {
        if (preferences == null) {
            preferences = new HashMap<>();
        }
        preferences.put(key, value);
    }
    
    /**
     * 获取偏好设置
     */
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
    
    /**
     * 设置元数据
     */
    public void setMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * 获取元数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        if (metadata == null) {
            return null;
        }
        Object value = metadata.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    // JPA 生命周期回调
    
    @PrePersist
    protected void prePersist() {
        if (loginCount == null) {
            loginCount = 0L;
        }
        if (preferences == null) {
            preferences = new HashMap<>();
        }
        if (metadata == null) {
            metadata = new HashMap<>();
        }
    }
    
    @PreUpdate
    protected void preUpdate() {
        // 可以在这里添加更新前的业务逻辑
    }
    
    @PostLoad
    protected void postLoad() {
        // 可以在这里添加加载后的业务逻辑
    }
    
    // Getters and Setters
    // ... (省略标准的 getter 和 setter 方法)
}

/**
 * 用户状态枚举
 */
public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    LOCKED("锁定"),
    PENDING("待审核"),
    SUSPENDED("暂停");
    
    private final String description;
    
    UserStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

/**
 * 性别枚举
 */
public enum Gender {
    MALE("男"),
    FEMALE("女"),
    OTHER("其他");
    
    private final String description;
    
    Gender(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
```

## 🔍 企业级 Repository 设计模式

### 1. 多层次 Repository 架构

```java
/**
 * 基础 Repository 接口
 * 定义通用的数据访问方法
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    /**
     * 根据 ID 查找未删除的实体
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    Optional<T> findByIdAndNotDeleted(@Param("id") ID id);
    
    /**
     * 查找所有未删除的实体
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false ORDER BY e.createdAt DESC")
    List<T> findAllNotDeleted();
    
    /**
     * 分页查找所有未删除的实体
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deleted = false")
    Page<T> findAllNotDeleted(Pageable pageable);
    
    /**
     * 软删除
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = :deletedAt, e.deletedBy = :deletedBy WHERE e.id = :id")
    int softDeleteById(@Param("id") ID id, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
    
    /**
     * 批量软删除
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = true, e.deletedAt = :deletedAt, e.deletedBy = :deletedBy WHERE e.id IN :ids")
    int softDeleteByIds(@Param("ids") Collection<ID> ids, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") String deletedBy);
    
    /**
     * 恢复软删除
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deleted = false, e.deletedAt = null, e.deletedBy = null WHERE e.id = :id")
    int restoreSoftDeleted(@Param("id") ID id);
    
    /**
     * 统计未删除的实体数量
     */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.deleted = false")
    long countNotDeleted();
    
    /**
     * 检查实体是否存在且未删除
     */
    @Query("SELECT COUNT(e) > 0 FROM #{#entityName} e WHERE e.id = :id AND e.deleted = false")
    boolean existsByIdAndNotDeleted(@Param("id") ID id);
}

/**
 * 用户 Repository 接口
 * 定义用户特定的查询方法
 */
public interface UserRepository extends BaseRepository<User, Long>, UserRepositoryCustom {
    
    // 基础查询方法
    
    Optional<User> findByUsernameAndDeletedFalse(String username);
    
    Optional<User> findByEmailAndDeletedFalse(String email);
    
    List<User> findByStatusAndDeletedFalse(UserStatus status);
    
    List<User> findByDepartmentIdAndDeletedFalse(Long departmentId);
    
    Page<User> findByStatusAndDeletedFalse(UserStatus status, Pageable pageable);
    
    // 复杂查询方法
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = :status AND u.department.id = :departmentId")
    List<User> findByStatusAndDepartment(@Param("status") UserStatus status, @Param("departmentId") Long departmentId);
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.age BETWEEN :minAge AND :maxAge ORDER BY u.age")
    List<User> findByAgeRange(@Param("minAge") int minAge, @Param("maxAge") int maxAge);
    
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.lastLoginAt < :date")
    List<User> findInactiveUsersSince(@Param("date") LocalDateTime date);
    
    // 使用 EntityGraph 优化查询
    
    @EntityGraph(value = "User.withDepartment", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.status = :status")
    List<User> findByStatusWithDepartment(@Param("status") UserStatus status);
    
    @EntityGraph(value = "User.withDepartmentAndRoles", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.id = :id")
    Optional<User> findByIdWithDepartmentAndRoles(@Param("id") Long id);
    
    @EntityGraph(value = "User.complete", type = EntityGraph.EntityGraphType.LOAD)
    @Query("SELECT u FROM User u WHERE u.deleted = false AND u.id = :id")
    Optional<User> findByIdComplete(@Param("id") Long id);
    
    // 统计查询
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.deleted = false AND u.status = :status")
    long countByStatus(@Param("status") UserStatus status);
    
    @Query("SELECT u.status, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.status")
    List<Object[]> getStatusStatistics();
    
    @Query("SELECT u.department.name, COUNT(u) FROM User u WHERE u.deleted = false GROUP BY u.department.name")
    List<Object[]> getDepartmentStatistics();
    
    // 更新操作
    
    @Modifying
    @Query("UPDATE User u SET u.status = :status WHERE u.id IN :ids")
    int updateStatusByIds(@Param("ids") Collection<Long> ids, @Param("status") UserStatus status);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime, u.loginCount = u.loginCount + 1 WHERE u.id = :id")
    int updateLoginInfo(@Param("id") Long id, @Param("loginTime") LocalDateTime loginTime);
    
    // 原生 SQL 查询
    
    @Query(value = "SELECT * FROM users u WHERE u.deleted = false AND MATCH(u.username, u.first_name, u.last_name) AGAINST (:keyword IN NATURAL LANGUAGE MODE)", nativeQuery = true)
    List<User> searchByKeyword(@Param("keyword") String keyword);
    
    @Query(value = """
        SELECT u.department_id, COUNT(*) as user_count, AVG(u.age) as avg_age 
        FROM users u 
        WHERE u.deleted = false 
        GROUP BY u.department_id 
        HAVING COUNT(*) > :minCount
        """, nativeQuery = true)
    List<Object[]> getDepartmentStatisticsWithMinCount(@Param("minCount") int minCount);
    
    // 投影查询
    
    @Query("SELECT new com.example.dto.UserSummaryDTO(u.id, u.username, u.email, u.status, u.department.name) FROM User u WHERE u.deleted = false")
    List<UserSummaryDTO> findAllSummaries();
    
    @Query("SELECT new com.example.dto.UserStatisticsDTO(u.department.name, COUNT(u), AVG(u.age)) FROM User u WHERE u.deleted = false GROUP BY u.department.name")
    List<UserStatisticsDTO> getUserStatisticsByDepartment();
    
    // 使用命名查询
    
    List<User> findActiveUsersByDepartment(@Param("departmentId") Long departmentId, @Param("status") UserStatus status);
    
    List<UserStatisticsDTO> findUserStatistics();
}

/**
 * 自定义 Repository 接口
 * 定义复杂的自定义查询方法
 */
public interface UserRepositoryCustom {
    
    /**
     * 动态条件查询
     */
    Page<User> findUsersWithCriteria(UserSearchCriteria criteria, Pageable pageable);
    
    /**
     * 复杂统计查询
     */
    List<UserStatisticsDTO> getUserStatistics(UserStatisticsQuery query);
    
    /**
     * 批量更新用户信息
     */
    int batchUpdateUsers(List<UserUpdateDTO> updates);
    
    /**
     * 获取用户活跃度报告
     */
    List<UserActivityReport> getUserActivityReport(LocalDate startDate, LocalDate endDate);
    
    /**
     * 复杂的用户搜索
     */
    Page<User> searchUsers(UserSearchQuery query, Pageable pageable);
}

/**
 * 自定义 Repository 实现
 */
@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public Page<User> findUsersWithCriteria(UserSearchCriteria criteria, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        
        // 构建查询
        CriteriaQuery<User> query = cb.createQuery(User.class);
        Root<User> root = query.from(User.class);
        
        // 构建条件
        List<Predicate> predicates = buildPredicates(cb, root, criteria);
        
        if (!predicates.isEmpty()) {
            query.where(predicates.toArray(new Predicate[0]));
        }
        
        // 添加排序
        List<Order> orders = buildOrders(cb, root, pageable.getSort());
        if (!orders.isEmpty()) {
            query.orderBy(orders);
        }
        
        // 执行查询
        TypedQuery<User> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<User> users = typedQuery.getResultList();
        
        // 获取总数
        long total = countUsersWithCriteria(criteria);
        
        return new PageImpl<>(users, pageable, total);
    }
    
    @Override
    public List<UserStatisticsDTO> getUserStatistics(UserStatisticsQuery query) {
        String jpql = """
            SELECT new com.example.dto.UserStatisticsDTO(
                u.department.name,
                COUNT(u),
                AVG(u.age),
                MIN(u.age),
                MAX(u.age),
                COUNT(CASE WHEN u.status = 'ACTIVE' THEN 1 END),
                COUNT(CASE WHEN u.lastLoginAt > :sinceDate THEN 1 END)
            )
            FROM User u 
            WHERE u.deleted = false
            """;
        
        if (query.getDepartmentIds() != null && !query.getDepartmentIds().isEmpty()) {
            jpql += " AND u.department.id IN :departmentIds";
        }
        
        if (query.getStatus() != null) {
            jpql += " AND u.status = :status";
        }
        
        jpql += " GROUP BY u.department.name";
        
        if (query.getMinUserCount() != null) {
            jpql += " HAVING COUNT(u) >= :minUserCount";
        }
        
        jpql += " ORDER BY COUNT(u) DESC";
        
        TypedQuery<UserStatisticsDTO> typedQuery = entityManager.createQuery(jpql, UserStatisticsDTO.class);
        
        // 设置参数
        typedQuery.setParameter("sinceDate", LocalDateTime.now().minusDays(30));
        
        if (query.getDepartmentIds() != null && !query.getDepartmentIds().isEmpty()) {
            typedQuery.setParameter("departmentIds", query.getDepartmentIds());
        }
        
        if (query.getStatus() != null) {
            typedQuery.setParameter("status", query.getStatus());
        }
        
        if (query.getMinUserCount() != null) {
            typedQuery.setParameter("minUserCount", query.getMinUserCount());
        }
        
        return typedQuery.getResultList();
    }
    
    @Override
    @Transactional
    public int batchUpdateUsers(List<UserUpdateDTO> updates) {
        int updateCount = 0;
        
        for (UserUpdateDTO update : updates) {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaUpdate<User> criteriaUpdate = cb.createCriteriaUpdate(User.class);
            Root<User> root = criteriaUpdate.from(User.class);
            
            // 构建更新条件
            criteriaUpdate.where(cb.equal(root.get("id"), update.getId()));
            
            // 设置更新字段
            if (update.getFirstName() != null) {
                criteriaUpdate.set(root.get("firstName"), update.getFirstName());
            }
            if (update.getLastName() != null) {
                criteriaUpdate.set(root.get("lastName"), update.getLastName());
            }
            if (update.getEmail() != null) {
                criteriaUpdate.set(root.get("email"), update.getEmail());
            }
            if (update.getStatus() != null) {
                criteriaUpdate.set(root.get("status"), update.getStatus());
            }
            if (update.getDepartmentId() != null) {
                criteriaUpdate.set(root.get("department"), entityManager.getReference(Department.class, update.getDepartmentId()));
            }
            
            // 设置更新时间
            criteriaUpdate.set(root.get("updatedAt"), LocalDateTime.now());
            
            updateCount += entityManager.createQuery(criteriaUpdate).executeUpdate();
        }
        
        return updateCount;
    }
    
    @Override
    public List<UserActivityReport> getUserActivityReport(LocalDate startDate, LocalDate endDate) {
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
        
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate.plusDays(1));
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        
        return results.stream()
                     .map(this::mapToUserActivityReport)
                     .collect(Collectors.toList());
    }
    
    @Override
    public Page<User> searchUsers(UserSearchQuery query, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<User> criteriaQuery = cb.createQuery(User.class);
        Root<User> root = criteriaQuery.from(User.class);
        
        // 添加关联
        if (query.isIncludeDepartment()) {
            root.fetch("department", JoinType.LEFT);
        }
        
        if (query.isIncludeRoles()) {
            root.fetch("roles", JoinType.LEFT);
        }
        
        // 构建搜索条件
        List<Predicate> predicates = new ArrayList<>();
        
        // 基础条件
        predicates.add(cb.equal(root.get("deleted"), false));
        
        // 文本搜索
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = "%" + query.getKeyword().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                cb.like(cb.lower(root.get("username")), keyword),
                cb.like(cb.lower(root.get("firstName")), keyword),
                cb.like(cb.lower(root.get("lastName")), keyword),
                cb.like(cb.lower(root.get("email")), keyword)
            );
            predicates.add(keywordPredicate);
        }
        
        // 状态过滤
        if (query.getStatuses() != null && !query.getStatuses().isEmpty()) {
            predicates.add(root.get("status").in(query.getStatuses()));
        }
        
        // 部门过滤
        if (query.getDepartmentIds() != null && !query.getDepartmentIds().isEmpty()) {
            predicates.add(root.get("department").get("id").in(query.getDepartmentIds()));
        }
        
        // 年龄范围
        if (query.getMinAge() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), query.getMinAge()));
        }
        if (query.getMaxAge() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("age"), query.getMaxAge()));
        }
        
        // 创建时间范围
        if (query.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), query.getCreatedAfter()));
        }
        if (query.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), query.getCreatedBefore()));
        }
        
        // 最后登录时间范围
        if (query.getLastLoginAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("lastLoginAt"), query.getLastLoginAfter()));
        }
        if (query.getLastLoginBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("lastLoginAt"), query.getLastLoginBefore()));
        }
        
        // 角色过滤
        if (query.getRoleNames() != null && !query.getRoleNames().isEmpty()) {
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            predicates.add(roleJoin.get("name").in(query.getRoleNames()));
        }
        
        criteriaQuery.where(predicates.toArray(new Predicate[0]));
        
        // 去重（如果有多个角色匹配）
        criteriaQuery.distinct(true);
        
        // 排序
        List<Order> orders = buildOrders(cb, root, pageable.getSort());
        if (!orders.isEmpty()) {
            criteriaQuery.orderBy(orders);
        } else {
            criteriaQuery.orderBy(cb.desc(root.get("lastLoginAt")));
        }
        
        // 执行查询
        TypedQuery<User> typedQuery = entityManager.createQuery(criteriaQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());
        
        List<User> users = typedQuery.getResultList();
        
        // 获取总数
        long total = countSearchUsers(query);
        
        return new PageImpl<>(users, pageable, total);
    }
    
    // 辅助方法
    
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<User> root, UserSearchCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        
        // 基础条件：未删除
        predicates.add(cb.equal(root.get("deleted"), false));
        
        // 用户名
        if (StringUtils.hasText(criteria.getUsername())) {
            predicates.add(cb.like(cb.lower(root.get("username")), 
                          "%" + criteria.getUsername().toLowerCase() + "%"));
        }
        
        // 邮箱
        if (StringUtils.hasText(criteria.getEmail())) {
            predicates.add(cb.like(cb.lower(root.get("email")), 
                          "%" + criteria.getEmail().toLowerCase() + "%"));
        }
        
        // 状态
        if (criteria.getStatus() != null) {
            predicates.add(cb.equal(root.get("status"), criteria.getStatus()));
        }
        
        // 部门
        if (criteria.getDepartmentId() != null) {
            predicates.add(cb.equal(root.get("department").get("id"), criteria.getDepartmentId()));
        }
        
        // 年龄范围
        if (criteria.getMinAge() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("age"), criteria.getMinAge()));
        }
        if (criteria.getMaxAge() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("age"), criteria.getMaxAge()));
        }
        
        // 创建时间范围
        if (criteria.getCreatedAfter() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.getCreatedAfter()));
        }
        if (criteria.getCreatedBefore() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.getCreatedBefore()));
        }
        
        return predicates;
    }
    
    private List<Order> buildOrders(CriteriaBuilder cb, Root<User> root, Sort sort) {
        List<Order> orders = new ArrayList<>();
        
        for (Sort.Order sortOrder : sort) {
            Path<?> path = getPath(root, sortOrder.getProperty());
            if (sortOrder.getDirection() == Sort.Direction.ASC) {
                orders.add(cb.asc(path));
            } else {
                orders.add(cb.desc(path));
            }
        }
        
        return orders;
    }
    
    private Path<?> getPath(Root<User> root, String property) {
        String[] parts = property.split("\\.");
        Path<?> path = root;
        
        for (String part : parts) {
            path = path.get(part);
        }
        
        return path;
    }
    
    private long countUsersWithCriteria(UserSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> root = countQuery.from(User.class);
        
        List<Predicate> predicates = buildPredicates(cb, root, criteria);
        
        countQuery.select(cb.count(root));
        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(new Predicate[0]));
        }
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    private long countSearchUsers(UserSearchQuery query) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<User> root = countQuery.from(User.class);
        
        // 构建与搜索相同的条件
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("deleted"), false));
        
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = "%" + query.getKeyword().toLowerCase() + "%";
            Predicate keywordPredicate = cb.or(
                cb.like(cb.lower(root.get("username")), keyword),
                cb.like(cb.lower(root.get("firstName")), keyword),
                cb.like(cb.lower(root.get("lastName")), keyword),
                cb.like(cb.lower(root.get("email")), keyword)
            );
            predicates.add(keywordPredicate);
        }
        
        // ... 其他条件与搜索方法相同
        
        if (query.getRoleNames() != null && !query.getRoleNames().isEmpty()) {
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            predicates.add(roleJoin.get("name").in(query.getRoleNames()));
            countQuery.select(cb.countDistinct(root));
        } else {
            countQuery.select(cb.count(root));
        }
        
        countQuery.where(predicates.toArray(new Predicate[0]));
        
        return entityManager.createQuery(countQuery).getSingleResult();
    }
    
    private UserActivityReport mapToUserActivityReport(Object[] row) {
        UserActivityReport report = new UserActivityReport();
        report.setUserId(((Number) row[0]).longValue());
        report.setUsername((String) row[1]);
        report.setEmail((String) row[2]);
        report.setDepartmentId(row[3] != null ? ((Number) row[3]).longValue() : null);
        report.setDepartmentName((String) row[4]);
        report.setLoginCount(row[5] != null ? ((Number) row[5]).longValue() : 0);
        report.setLastLoginAt(row[6] != null ? ((Timestamp) row[6]).toLocalDateTime() : null);
        report.setOrderCount(((Number) row[7]).intValue());
        report.setTotalAmount((BigDecimal) row[8]);
        report.setActivityLevel(ActivityLevel.valueOf((String) row[9]));
        return report;
    }
}
```

## 📝 小结

Spring Data JPA 企业级实战涵盖了从基础实体设计到复杂查询优化的全方位技术实现，通过合理的架构设计和最佳实践，可以构建出高效、可维护的企业级数据访问层。

### 核心特性总结

- **实体设计** - 完善的实体关系映射和生命周期管理
- **Repository 模式** - 多层次的 Repository 架构设计
- **查询优化** - EntityGraph、Criteria API、投影查询等优化技术
- **缓存集成** - 一级缓存、二级缓存、查询缓存的合理使用
- **审计功能** - 自动审计字段管理和软删除支持

### 最佳实践要点

1. **实体设计原则** - 合理的关系映射和性能优化
2. **查询策略** - 根据场景选择合适的查询方式
3. **缓存策略** - 分层缓存设计和缓存一致性保证
4. **性能监控** - 建立完善的 SQL 性能监控体系
5. **事务管理** - 合理的事务边界控制和隔离级别设置

通过深入掌握 Spring Data JPA 的企业级应用模式，开发者可以构建出既高效又可扩展的数据访问层，为复杂的企业级应用提供强有力的数据支撑。
