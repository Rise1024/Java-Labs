package com.javalaabs.webflux.monitoring;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控组件
 * 收集和记录应用性能指标
 */
@Component
public class PerformanceMonitor {
    
    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    private final Counter errorCounter;
    private final Gauge activeConnections;
    private final AtomicLong activeConnectionCount;
    
    // 自定义指标
    private final Timer userServiceTimer;
    private final Counter userCreatedCounter;
    private final Counter userDeletedCounter;
    private final DistributionSummary responseSize;
    
    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeConnectionCount = new AtomicLong(0);
        
        // 基础指标
        this.requestTimer = Timer.builder("webflux.request.duration")
                                .description("WebFlux request duration")
                                .register(meterRegistry);
        
        this.requestCounter = Counter.builder("webflux.request.count")
                                   .description("WebFlux request count")
                                   .register(meterRegistry);
        
        this.errorCounter = Counter.builder("webflux.error.count")
                                 .description("WebFlux error count")
                                 .register(meterRegistry);
        
        this.activeConnections = Gauge.builder("webflux.connections.active", this, obj -> (double) obj.getActiveConnectionCount())
                                     .description("Active WebFlux connections")
                                     .register(meterRegistry);
        
        // 业务指标
        this.userServiceTimer = Timer.builder("user.service.duration")
                                    .description("User service operation duration")
                                    .register(meterRegistry);
        
        this.userCreatedCounter = Counter.builder("user.created.count")
                                        .description("Number of users created")
                                        .register(meterRegistry);
        
        this.userDeletedCounter = Counter.builder("user.deleted.count")
                                        .description("Number of users deleted")
                                        .register(meterRegistry);
        
        this.responseSize = DistributionSummary.builder("webflux.response.size")
                                             .description("Response size in bytes")
                                             .register(meterRegistry);
    }
    
    /**
     * 开始计时器
     */
    public Timer.Sample startTimer() {
        requestCounter.increment();
        return Timer.start(meterRegistry);
    }
    
    /**
     * 记录请求处理时间和状态
     */
    public void recordRequest(Timer.Sample sample, String method, String uri, int statusCode) {
        sample.stop(Timer.builder("webflux.request.duration")
                        .tag("method", method)
                        .tag("uri", sanitizeUri(uri))
                        .tag("status", String.valueOf(statusCode))
                        .register(meterRegistry));
        
        // 记录错误
        if (statusCode >= 400) {
            Counter.builder("webflux.error.count")
                   .tag("method", method)
                   .tag("status", String.valueOf(statusCode))
                   .register(meterRegistry)
                   .increment();
        }
    }
    
    /**
     * 记录用户服务操作
     */
    public Timer.Sample startUserServiceTimer(String operation) {
        return Timer.start(meterRegistry);
    }
    
    public void recordUserServiceOperation(Timer.Sample sample, String operation, boolean success) {
        sample.stop(Timer.builder("user.service.duration")
                        .tag("operation", operation)
                        .tag("success", String.valueOf(success))
                        .register(meterRegistry));
    }
    
    /**
     * 记录用户创建
     */
    public void recordUserCreated() {
        userCreatedCounter.increment();
    }
    
    /**
     * 记录用户删除
     */
    public void recordUserDeleted() {
        userDeletedCounter.increment();
    }
    
    /**
     * 记录响应大小
     */
    public void recordResponseSize(long size) {
        responseSize.record(size);
    }
    
    /**
     * 增加活跃连接数
     */
    public void incrementActiveConnections() {
        activeConnectionCount.incrementAndGet();
    }
    
    /**
     * 减少活跃连接数
     */
    public void decrementActiveConnections() {
        activeConnectionCount.decrementAndGet();
    }
    
    /**
     * 获取活跃连接数
     */
    public double getActiveConnectionCount() {
        return activeConnectionCount.get();
    }
    
    /**
     * 记录自定义指标
     */
    public void recordCustomMetric(String name, double value, String... tags) {
        Gauge.builder(name, this, obj -> value)
             .tags(tags)
             .register(meterRegistry);
    }
    
    /**
     * 增加自定义计数器
     */
    public void incrementCustomCounter(String name, String... tags) {
        Counter.builder(name)
               .tags(tags)
               .register(meterRegistry)
               .increment();
    }
    
    /**
     * 记录自定义计时器
     */
    public void recordCustomTimer(String name, Duration duration, String... tags) {
        Timer.builder(name)
             .tags(tags)
             .register(meterRegistry)
             .record(duration);
    }
    
    /**
     * 获取系统指标
     */
    public SystemMetrics getSystemMetrics() {
        Runtime runtime = Runtime.getRuntime();
        
        return SystemMetrics.builder()
                           .totalMemory(runtime.totalMemory())
                           .freeMemory(runtime.freeMemory())
                           .usedMemory(runtime.totalMemory() - runtime.freeMemory())
                           .maxMemory(runtime.maxMemory())
                           .processors(runtime.availableProcessors())
                           .timestamp(Instant.now())
                           .build();
    }
    
    /**
     * 获取应用指标摘要
     */
    public ApplicationMetrics getApplicationMetrics() {
        return ApplicationMetrics.builder()
                                .totalRequests(requestCounter.count())
                                .totalErrors(errorCounter.count())
                                .activeConnections(activeConnectionCount.get())
                                .usersCreated(userCreatedCounter.count())
                                .usersDeleted(userDeletedCounter.count())
                                .averageResponseTime(requestTimer.mean(TimeUnit.MILLISECONDS))
                                .timestamp(Instant.now())
                                .build();
    }
    
    /**
     * 清理URI中的动态部分，用于标签
     */
    private String sanitizeUri(String uri) {
        if (uri == null) return "unknown";
        
        // 替换动态路径参数
        return uri.replaceAll("/[0-9a-fA-F-]{36}", "/{id}")  // UUID
                 .replaceAll("/\\d+", "/{id}")               // 数字ID
                 .replaceAll("/[^/]+@[^/]+", "/{email}");    // 邮箱
    }
    
    // 内部类：系统指标
    public static class SystemMetrics {
        private final long totalMemory;
        private final long freeMemory;
        private final long usedMemory;
        private final long maxMemory;
        private final int processors;
        private final Instant timestamp;
        
        private SystemMetrics(Builder builder) {
            this.totalMemory = builder.totalMemory;
            this.freeMemory = builder.freeMemory;
            this.usedMemory = builder.usedMemory;
            this.maxMemory = builder.maxMemory;
            this.processors = builder.processors;
            this.timestamp = builder.timestamp;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private long totalMemory;
            private long freeMemory;
            private long usedMemory;
            private long maxMemory;
            private int processors;
            private Instant timestamp;
            
            public Builder totalMemory(long totalMemory) {
                this.totalMemory = totalMemory;
                return this;
            }
            
            public Builder freeMemory(long freeMemory) {
                this.freeMemory = freeMemory;
                return this;
            }
            
            public Builder usedMemory(long usedMemory) {
                this.usedMemory = usedMemory;
                return this;
            }
            
            public Builder maxMemory(long maxMemory) {
                this.maxMemory = maxMemory;
                return this;
            }
            
            public Builder processors(int processors) {
                this.processors = processors;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public SystemMetrics build() {
                return new SystemMetrics(this);
            }
        }
        
        // Getters
        public long getTotalMemory() { return totalMemory; }
        public long getFreeMemory() { return freeMemory; }
        public long getUsedMemory() { return usedMemory; }
        public long getMaxMemory() { return maxMemory; }
        public int getProcessors() { return processors; }
        public Instant getTimestamp() { return timestamp; }
    }
    
    // 内部类：应用指标
    public static class ApplicationMetrics {
        private final double totalRequests;
        private final double totalErrors;
        private final long activeConnections;
        private final double usersCreated;
        private final double usersDeleted;
        private final double averageResponseTime;
        private final Instant timestamp;
        
        private ApplicationMetrics(Builder builder) {
            this.totalRequests = builder.totalRequests;
            this.totalErrors = builder.totalErrors;
            this.activeConnections = builder.activeConnections;
            this.usersCreated = builder.usersCreated;
            this.usersDeleted = builder.usersDeleted;
            this.averageResponseTime = builder.averageResponseTime;
            this.timestamp = builder.timestamp;
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private double totalRequests;
            private double totalErrors;
            private long activeConnections;
            private double usersCreated;
            private double usersDeleted;
            private double averageResponseTime;
            private Instant timestamp;
            
            public Builder totalRequests(double totalRequests) {
                this.totalRequests = totalRequests;
                return this;
            }
            
            public Builder totalErrors(double totalErrors) {
                this.totalErrors = totalErrors;
                return this;
            }
            
            public Builder activeConnections(long activeConnections) {
                this.activeConnections = activeConnections;
                return this;
            }
            
            public Builder usersCreated(double usersCreated) {
                this.usersCreated = usersCreated;
                return this;
            }
            
            public Builder usersDeleted(double usersDeleted) {
                this.usersDeleted = usersDeleted;
                return this;
            }
            
            public Builder averageResponseTime(double averageResponseTime) {
                this.averageResponseTime = averageResponseTime;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                this.timestamp = timestamp;
                return this;
            }
            
            public ApplicationMetrics build() {
                return new ApplicationMetrics(this);
            }
        }
        
        // Getters
        public double getTotalRequests() { return totalRequests; }
        public double getTotalErrors() { return totalErrors; }
        public long getActiveConnections() { return activeConnections; }
        public double getUsersCreated() { return usersCreated; }
        public double getUsersDeleted() { return usersDeleted; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public Instant getTimestamp() { return timestamp; }
        
        public double getErrorRate() {
            return totalRequests > 0 ? (totalErrors / totalRequests) * 100 : 0;
        }
    }
}
