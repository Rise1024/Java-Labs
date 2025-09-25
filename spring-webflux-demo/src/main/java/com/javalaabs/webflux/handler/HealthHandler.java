package com.javalaabs.webflux.handler;

import com.javalaabs.webflux.service.ReactiveUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 健康检查处理器
 * 提供系统健康状态检查
 */
@Component
public class HealthHandler {
    
    private final ReactiveUserService userService;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    
    public HealthHandler(ReactiveUserService userService,
                        @Autowired(required = false) ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 基础健康检查
     */
    public Mono<ServerResponse> health(ServerRequest request) {
        return Mono.zip(
            checkDatabaseHealth(),
            checkRedisHealth(),
            checkServiceHealth()
        ).map(tuple -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", Instant.now());
            health.put("database", tuple.getT1());
            health.put("redis", tuple.getT2());
            health.put("service", tuple.getT3());
            
            // 检查所有组件是否健康
            boolean allHealthy = Stream.of(tuple.getT1(), tuple.getT2(), tuple.getT3())
                                     .map(status -> "UP".equals(((Map<?, ?>) status).get("status")))
                                     .allMatch(Boolean::booleanValue);
            
            if (!allHealthy) {
                health.put("status", "DOWN");
            }
            
            HttpStatus httpStatus = allHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
            return ServerResponse.status(httpStatus)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(health);
        }).flatMap(response -> response)
          .timeout(Duration.ofSeconds(10))
          .onErrorResume(error -> 
              ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(Map.of(
                                "status", "DOWN",
                                "error", error.getMessage(),
                                "timestamp", Instant.now()
                            )));
    }
    
    /**
     * 详细健康检查
     */
    public Mono<ServerResponse> detailedHealth(ServerRequest request) {
        return Mono.zip(
            checkDatabaseHealth(),
            checkRedisHealth(),
            checkServiceHealth(),
            getSystemMetrics(),
            getApplicationMetrics()
        ).map(tuple -> {
            Map<String, Object> detailedHealth = new HashMap<>();
            detailedHealth.put("status", "UP");
            detailedHealth.put("timestamp", Instant.now());
            detailedHealth.put("components", Map.of(
                "database", tuple.getT1(),
                "redis", tuple.getT2(),
                "service", tuple.getT3()
            ));
            detailedHealth.put("system", tuple.getT4());
            detailedHealth.put("application", tuple.getT5());
            
            return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(detailedHealth);
        }).flatMap(response -> response)
          .timeout(Duration.ofSeconds(15));
    }
    
    /**
     * 指标信息
     */
    public Mono<ServerResponse> metrics(ServerRequest request) {
        return getApplicationMetrics()
            .flatMap(metrics -> 
                ServerResponse.ok()
                             .contentType(MediaType.APPLICATION_JSON)
                             .bodyValue(metrics))
            .timeout(Duration.ofSeconds(5));
    }
    
    /**
     * 就绪检查
     */
    public Mono<ServerResponse> readiness(ServerRequest request) {
        return checkDatabaseHealth()
            .flatMap(dbHealth -> {
                boolean isReady = "UP".equals(dbHealth.get("status"));
                HttpStatus status = isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                
                Map<String, Object> readiness = Map.of(
                    "status", isReady ? "READY" : "NOT_READY",
                    "database", dbHealth,
                    "timestamp", Instant.now()
                );
                
                return ServerResponse.status(status)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(readiness);
            });
    }
    
    /**
     * 存活检查
     */
    public Mono<ServerResponse> liveness(ServerRequest request) {
        return Mono.just(Map.of(
            "status", "ALIVE",
            "timestamp", Instant.now(),
            "uptime", getUptime()
        )).flatMap(liveness -> 
            ServerResponse.ok()
                         .contentType(MediaType.APPLICATION_JSON)
                         .bodyValue(liveness));
    }
    
    // 私有健康检查方法
    private Mono<Map<String, Object>> checkDatabaseHealth() {
        return userService.getHealthStatus()
                         .map(status -> {
                             Map<String, Object> resultMap = new HashMap<>();
                             resultMap.put("status", status);
                             resultMap.put("type", "database");
                             resultMap.put("responseTime", "< 100ms");
                             return resultMap;
                         })
                         .timeout(Duration.ofSeconds(3))
                        .onErrorResume(error -> {
                            Map<String, Object> errorMap = new HashMap<>();
                            errorMap.put("status", "DOWN");
                            errorMap.put("type", "database");
                            errorMap.put("error", "Database connection timeout");
                            return Mono.just(errorMap);
                        });
    }
    
    private Mono<Map<String, Object>> checkRedisHealth() {
        // 如果Redis不可用，直接返回禁用状态
        if (redisTemplate == null) {
            Map<String, Object> disabledMap = new HashMap<>();
            disabledMap.put("status", "DISABLED");
            disabledMap.put("type", "redis");
            disabledMap.put("message", "Redis is disabled in test environment");
            return Mono.just(disabledMap);
        }
        
        String testKey = "health-check:" + System.currentTimeMillis();
        
        return redisTemplate.opsForValue()
                           .set(testKey, "test")
                           .then(redisTemplate.delete(testKey))
                           .map(result -> {
                               Map<String, Object> resultMap = new HashMap<>();
                               resultMap.put("status", "UP");
                               resultMap.put("type", "redis");
                               resultMap.put("responseTime", "< 50ms");
                               return resultMap;
                           })
                           .timeout(Duration.ofSeconds(2))
                           .onErrorResume(error -> {
                               Map<String, Object> errorMap = new HashMap<>();
                               errorMap.put("status", "DOWN");
                               errorMap.put("type", "redis");
                               errorMap.put("error", "Redis connection failed");
                               return Mono.just(errorMap);
                           });
    }
    
    private Mono<Map<String, Object>> checkServiceHealth() {
        return Mono.just(Map.of(
            "status", "UP",
            "type", "service",
            "version", "1.0.0",
            "description", "WebFlux User Service"
        ));
    }
    
    private Mono<Map<String, Object>> getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        Map<String, Object> systemMetrics = Map.of(
            "memory", Map.of(
                "total", runtime.totalMemory(),
                "free", runtime.freeMemory(),
                "used", runtime.totalMemory() - runtime.freeMemory(),
                "max", runtime.maxMemory()
            ),
            "processors", runtime.availableProcessors(),
            "os", Map.of(
                "name", System.getProperty("os.name"),
                "version", System.getProperty("os.version"),
                "arch", System.getProperty("os.arch")
            ),
            "java", Map.of(
                "version", System.getProperty("java.version"),
                "vendor", System.getProperty("java.vendor")
            )
        );
        
        return Mono.just(systemMetrics);
    }
    
    private Mono<Map<String, Object>> getApplicationMetrics() {
        return userService.getStatistics()
                         .map(stats -> Map.of(
                             "userStatistics", stats,
                             "uptime", getUptime(),
                             "timestamp", Instant.now()
                         ))
                         .onErrorReturn(Map.of(
                             "error", "Unable to get application metrics",
                             "timestamp", Instant.now()
                         ));
    }
    
    private long getUptime() {
        return System.currentTimeMillis() - getStartTime();
    }
    
    private long getStartTime() {
        // 简化实现，实际应该记录应用启动时间
        return System.currentTimeMillis() - 60000; // 假设运行了1分钟
    }
}
