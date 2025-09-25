package com.javalaabs.webflux.repository;

import com.javalaabs.webflux.domain.entity.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 用户响应式Repository接口
 * 基于R2DBC实现响应式数据库访问
 */
@Repository
public interface ReactiveUserRepository extends R2dbcRepository<User, String> {
    
    /**
     * 根据邮箱查找用户
     */
    Mono<User> findByEmail(String email);
    
    /**
     * 检查邮箱是否存在
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * 根据账户类型查找用户
     */
    Flux<User> findByAccountType(String accountType);
    
    /**
     * 查找活跃用户
     */
    Flux<User> findByIsActiveTrue();
    
    /**
     * 根据名称模糊查询用户
     */
    @Query("SELECT * FROM users WHERE name LIKE CONCAT('%', :name, '%')")
    Flux<User> findByNameContaining(@Param("name") String name);
    
    /**
     * 根据邮箱模糊查询用户
     */
    @Query("SELECT * FROM users WHERE email LIKE CONCAT('%', :email, '%')")
    Flux<User> findByEmailContaining(@Param("email") String email);
    
    /**
     * 组合查询：根据名称或邮箱模糊查询
     */
    @Query("SELECT * FROM users WHERE name LIKE CONCAT('%', :search, '%') OR email LIKE CONCAT('%', :search, '%')")
    Flux<User> findByNameOrEmailContaining(@Param("search") String search);
    
    /**
     * 查找指定时间之后创建的用户
     */
    Flux<User> findByCreateTimeAfter(Instant createTime);
    
    /**
     * 查找指定时间之后更新的用户
     */
    Flux<User> findByUpdateTimeAfter(Instant updateTime);
    
    /**
     * 分页查询用户（按创建时间降序）
     */
    @Query("SELECT * FROM users ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
    Flux<User> findUsersWithPaging(@Param("limit") int limit, @Param("offset") long offset);
    
    /**
     * 分页查询活跃用户
     */
    @Query("SELECT * FROM users WHERE is_active = true ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
    Flux<User> findActiveUsersWithPaging(@Param("limit") int limit, @Param("offset") long offset);
    
    /**
     * 根据账户类型分页查询
     */
    @Query("SELECT * FROM users WHERE account_type = :accountType ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
    Flux<User> findByAccountTypeWithPaging(@Param("accountType") String accountType, 
                                          @Param("limit") int limit, 
                                          @Param("offset") long offset);
    
    /**
     * 搜索用户（支持名称、邮箱模糊查询+分页）
     */
    @Query("SELECT * FROM users WHERE " +
           "(name LIKE CONCAT('%', :search, '%') OR email LIKE CONCAT('%', :search, '%')) " +
           "ORDER BY create_time DESC LIMIT :limit OFFSET :offset")
    Flux<User> searchUsersWithPaging(@Param("search") String search, 
                                    @Param("limit") int limit, 
                                    @Param("offset") long offset);
    
    /**
     * 统计用户总数
     */
    @Query("SELECT COUNT(*) FROM users")
    Mono<Long> countUsers();
    
    /**
     * 统计活跃用户数
     */
    @Query("SELECT COUNT(*) FROM users WHERE is_active = true")
    Mono<Long> countActiveUsers();
    
    /**
     * 根据账户类型统计用户数
     */
    @Query("SELECT COUNT(*) FROM users WHERE account_type = :accountType")
    Mono<Long> countByAccountType(@Param("accountType") String accountType);
    
    /**
     * 批量删除指定时间之前的非活跃用户
     */
    @Query("DELETE FROM users WHERE is_active = false AND update_time < :beforeTime")
    Mono<Integer> deleteInactiveUsersBefore(@Param("beforeTime") Instant beforeTime);
    
    /**
     * 批量更新用户的账户类型
     */
    @Query("UPDATE users SET account_type = :newAccountType, update_time = :updateTime WHERE account_type = :oldAccountType")
    Mono<Integer> updateAccountType(@Param("oldAccountType") String oldAccountType, 
                                   @Param("newAccountType") String newAccountType,
                                   @Param("updateTime") Instant updateTime);
    
    /**
     * 根据ID批量查询用户
     */
    @Query("SELECT * FROM users WHERE id IN (:ids)")
    Flux<User> findByIdIn(@Param("ids") Flux<String> ids);
    
    /**
     * 查找最近注册的用户
     */
    @Query("SELECT * FROM users ORDER BY create_time DESC LIMIT :limit")
    Flux<User> findRecentUsers(@Param("limit") int limit);
    
    /**
     * 查找最近活跃的用户
     */
    @Query("SELECT * FROM users WHERE is_active = true ORDER BY update_time DESC LIMIT :limit")
    Flux<User> findRecentActiveUsers(@Param("limit") int limit);
}
