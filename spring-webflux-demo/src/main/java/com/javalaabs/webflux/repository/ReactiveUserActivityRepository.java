package com.javalaabs.webflux.repository;

import com.javalaabs.webflux.domain.entity.UserActivity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

/**
 * 用户活动响应式Repository接口
 */
@Repository
public interface ReactiveUserActivityRepository extends R2dbcRepository<UserActivity, String> {
    
    /**
     * 根据用户ID查找活动记录
     */
    Flux<UserActivity> findByUserId(String userId);
    
    /**
     * 根据用户ID和操作类型查找活动记录
     */
    Flux<UserActivity> findByUserIdAndAction(String userId, String action);
    
    /**
     * 根据操作类型查找活动记录
     */
    Flux<UserActivity> findByAction(String action);
    
    /**
     * 查找指定时间之后的活动记录
     */
    Flux<UserActivity> findByTimestampAfter(Instant timestamp);
    
    /**
     * 查找指定时间范围内的活动记录
     */
    Flux<UserActivity> findByTimestampBetween(Instant startTime, Instant endTime);
    
    /**
     * 查找用户在指定时间之后的活动
     */
    Flux<UserActivity> findByUserIdAndTimestampAfter(String userId, Instant timestamp);
    
    /**
     * 查找最近的用户活动（用于实时流）
     */
    @Query("SELECT * FROM user_activities WHERE timestamp > :afterTime ORDER BY timestamp DESC")
    Flux<UserActivity> findRecentActivities(@Param("afterTime") Instant afterTime);
    
    /**
     * 查找用户最近的活动记录
     */
    @Query("SELECT * FROM user_activities WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit")
    Flux<UserActivity> findUserRecentActivities(@Param("userId") String userId, @Param("limit") int limit);
    
    /**
     * 分页查询用户活动
     */
    @Query("SELECT * FROM user_activities WHERE user_id = :userId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    Flux<UserActivity> findUserActivitiesWithPaging(@Param("userId") String userId, 
                                                   @Param("limit") int limit, 
                                                   @Param("offset") long offset);
    
    /**
     * 查找热门活动类型（按数量统计）
     */
    @Query("SELECT action, COUNT(*) as count FROM user_activities GROUP BY action ORDER BY count DESC LIMIT :limit")
    Flux<Object[]> findTopActionsByCount(@Param("limit") int limit);
    
    /**
     * 统计用户活动总数
     */
    @Query("SELECT COUNT(*) FROM user_activities WHERE user_id = :userId")
    Mono<Long> countByUserId(@Param("userId") String userId);
    
    /**
     * 统计指定操作的次数
     */
    @Query("SELECT COUNT(*) FROM user_activities WHERE user_id = :userId AND action = :action")
    Mono<Long> countByUserIdAndAction(@Param("userId") String userId, @Param("action") String action);
    
    /**
     * 统计指定时间范围内的活动数
     */
    @Query("SELECT COUNT(*) FROM user_activities WHERE timestamp BETWEEN :startTime AND :endTime")
    Mono<Long> countByTimestampBetween(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
    
    /**
     * 删除指定时间之前的活动记录
     */
    @Query("DELETE FROM user_activities WHERE timestamp < :beforeTime")
    Mono<Integer> deleteActivitiesBefore(@Param("beforeTime") Instant beforeTime);
    
    /**
     * 删除用户的所有活动记录
     */
    @Query("DELETE FROM user_activities WHERE user_id = :userId")
    Mono<Integer> deleteByUserId(@Param("userId") String userId);
    
    /**
     * 查找活跃用户（基于最近活动）
     */
    @Query("SELECT DISTINCT user_id FROM user_activities WHERE timestamp > :afterTime")
    Flux<String> findActiveUserIds(@Param("afterTime") Instant afterTime);
    
    /**
     * 查找用户最后一次活动时间
     */
    @Query("SELECT MAX(timestamp) FROM user_activities WHERE user_id = :userId")
    Mono<Instant> findLastActivityTime(@Param("userId") String userId);
    
    /**
     * 查找指定IP地址的活动记录
     */
    @Query("SELECT * FROM user_activities WHERE ip_address = :ipAddress ORDER BY timestamp DESC")
    Flux<UserActivity> findByIpAddress(@Param("ipAddress") String ipAddress);
    
    /**
     * 统计每日活动数量
     */
    @Query("SELECT DATE(timestamp) as activity_date, COUNT(*) as count " +
           "FROM user_activities " +
           "WHERE timestamp >= :startDate " +
           "GROUP BY DATE(timestamp) " +
           "ORDER BY activity_date DESC")
    Flux<Object[]> getDailyActivityCounts(@Param("startDate") Instant startDate);
    
    /**
     * 查找用户会话（登录到登出之间的活动）
     */
    @Query("SELECT * FROM user_activities " +
           "WHERE user_id = :userId " +
           "AND timestamp BETWEEN :sessionStart AND :sessionEnd " +
           "ORDER BY timestamp ASC")
    Flux<UserActivity> findUserSession(@Param("userId") String userId, 
                                      @Param("sessionStart") Instant sessionStart, 
                                      @Param("sessionEnd") Instant sessionEnd);
}
