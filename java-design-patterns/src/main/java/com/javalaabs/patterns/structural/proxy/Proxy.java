package com.javalaabs.patterns.structural.proxy;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 代理类 - 控制对真实主题的访问
 * 
 * @author JavaLabs
 */
@Slf4j
public class Proxy implements Subject {
    
    private RealSubject realSubject;
    private final Map<String, String> cache = new HashMap<>();
    
    @Override
    public String request(String request) {
        log.info("代理接收请求: {}", request);
        
        // 1. 预处理 - 验证访问权限
        if (!checkAccess(request)) {
            return "访问被拒绝: " + request;
        }
        
        // 2. 缓存检查
        if (cache.containsKey(request)) {
            String cachedResult = cache.get(request);
            log.info("从缓存返回结果: {}", cachedResult);
            return cachedResult;
        }
        
        // 3. 延迟初始化真实对象
        if (realSubject == null) {
            log.info("延迟创建真实主题对象");
            realSubject = new RealSubject();
        }
        
        // 4. 调用真实对象
        String result = realSubject.request(request);
        
        // 5. 后处理 - 缓存结果
        cache.put(request, result);
        log.info("缓存处理结果: {}", request);
        
        return result;
    }
    
    /**
     * 访问权限检查
     * 
     * @param request 请求
     * @return 是否允许访问
     */
    private boolean checkAccess(String request) {
        // 简单的权限检查示例
        boolean allowed = !request.contains("forbidden");
        log.info("权限检查 [{}]: {}", request, allowed ? "允许" : "拒绝");
        return allowed;
    }
}
