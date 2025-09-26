package com.javalaabs.patterns.structural.proxy;

import com.javalaabs.patterns.utils.PatternUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 真实主题 - 实际的业务对象
 * 
 * @author JavaLabs
 */
@Slf4j
public class RealSubject implements Subject {
    
    @Override
    public String request(String request) {
        log.info("真实主题处理请求: {}", request);
        
        // 模拟耗时操作
        PatternUtils.delay(1000);
        
        String response = "真实处理结果: " + request.toUpperCase();
        log.info("真实主题处理完成: {}", response);
        
        return response;
    }
}
