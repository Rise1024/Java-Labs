package com.javalaabs.webflux.monitoring;

import io.micrometer.core.instrument.Timer;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 请求监控过滤器
 * 自动监控所有HTTP请求的性能指标
 */
@Component
@Order(-1) // 高优先级，确保在其他过滤器之前执行
public class RequestMonitoringFilter implements WebFilter {
    
    private static final String REQUEST_ID_ATTR = "requestId";
    private static final String START_TIME_ATTR = "startTime";
    private static final String TIMER_SAMPLE_ATTR = "timerSample";
    
    private final PerformanceMonitor performanceMonitor;
    
    public RequestMonitoringFilter(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 生成请求ID
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put(REQUEST_ID_ATTR, requestId);
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        exchange.getAttributes().put(START_TIME_ATTR, startTime);
        
        // 开始计时
        Timer.Sample timerSample = performanceMonitor.startTimer();
        exchange.getAttributes().put(TIMER_SAMPLE_ATTR, timerSample);
        
        // 增加活跃连接数
        performanceMonitor.incrementActiveConnections();
        
        // 记录请求开始日志
        logRequestStart(request, requestId);
        
        return chain.filter(exchange)
                   .doOnSuccess(result -> handleRequestSuccess(exchange))
                   .doOnError(error -> handleRequestError(exchange, error))
                   .doFinally(signalType -> {
                       // 减少活跃连接数
                       performanceMonitor.decrementActiveConnections();
                       
                       // 记录请求完成日志
                       logRequestEnd(exchange, signalType.toString());
                   });
    }
    
    /**
     * 处理请求成功
     */
    private void handleRequestSuccess(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        long duration = calculateDuration(exchange);
        String requestId = getRequestId(exchange);
        Timer.Sample timerSample = getTimerSample(exchange);
        
        // 记录性能指标
        if (timerSample != null) {
            performanceMonitor.recordRequest(
                timerSample,
                request.getMethod().name(),
                request.getPath().value(),
                response.getStatusCode() != null ? response.getStatusCode().value() : 200
            );
        }
        
        // 记录响应大小（如果可用）
        if (response.getHeaders().getContentLength() > 0) {
            performanceMonitor.recordResponseSize(response.getHeaders().getContentLength());
        }
        
        System.out.println(String.format(
            "请求完成: %s %s - %dms - %s - RequestId: %s",
            request.getMethod(),
            request.getPath(),
            duration,
            response.getStatusCode(),
            requestId
        ));
    }
    
    /**
     * 处理请求错误
     */
    private void handleRequestError(ServerWebExchange exchange, Throwable error) {
        ServerHttpRequest request = exchange.getRequest();
        
        long duration = calculateDuration(exchange);
        String requestId = getRequestId(exchange);
        Timer.Sample timerSample = getTimerSample(exchange);
        
        // 记录性能指标（假设是500错误）
        if (timerSample != null) {
            performanceMonitor.recordRequest(
                timerSample,
                request.getMethod().name(),
                request.getPath().value(),
                500
            );
        }
        
        System.err.println(String.format(
            "请求异常: %s %s - %dms - RequestId: %s - Error: %s",
            request.getMethod(),
            request.getPath(),
            duration,
            requestId,
            error.getMessage()
        ));
    }
    
    /**
     * 记录请求开始日志
     */
    private void logRequestStart(ServerHttpRequest request, String requestId) {
        System.out.println(String.format(
            "请求开始: %s %s - RequestId: %s - RemoteAddress: %s - UserAgent: %s",
            request.getMethod(),
            request.getURI(),
            requestId,
            getRemoteAddress(request),
            getUserAgent(request)
        ));
    }
    
    /**
     * 记录请求结束日志
     */
    private void logRequestEnd(ServerWebExchange exchange, String signalType) {
        ServerHttpRequest request = exchange.getRequest();
        long duration = calculateDuration(exchange);
        String requestId = getRequestId(exchange);
        
        System.out.println(String.format(
            "请求结束: %s %s - %dms - RequestId: %s - Signal: %s",
            request.getMethod(),
            request.getPath(),
            duration,
            requestId,
            signalType
        ));
    }
    
    /**
     * 计算请求持续时间
     */
    private long calculateDuration(ServerWebExchange exchange) {
        Long startTime = (Long) exchange.getAttributes().get(START_TIME_ATTR);
        return startTime != null ? System.currentTimeMillis() - startTime : 0;
    }
    
    /**
     * 获取请求ID
     */
    private String getRequestId(ServerWebExchange exchange) {
        Object requestId = exchange.getAttributes().get(REQUEST_ID_ATTR);
        return requestId != null ? requestId.toString() : "unknown";
    }
    
    /**
     * 获取计时器样本
     */
    private Timer.Sample getTimerSample(ServerWebExchange exchange) {
        return (Timer.Sample) exchange.getAttributes().get(TIMER_SAMPLE_ATTR);
    }
    
    /**
     * 获取远程地址
     */
    private String getRemoteAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null ? 
            request.getRemoteAddress().getAddress().getHostAddress() : "unknown";
    }
    
    /**
     * 获取用户代理
     */
    private String getUserAgent(ServerHttpRequest request) {
        String userAgent = request.getHeaders().getFirst("User-Agent");
        return userAgent != null ? userAgent : "unknown";
    }
}
