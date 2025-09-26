package com.javalaabs.patterns.behavioral.strategy;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 上下文类 - 策略模式的环境类
 * 
 * @author JavaLabs
 */
@Slf4j
@AllArgsConstructor
public class Context {
    
    private Strategy strategy;
    
    /**
     * 设置策略
     * 
     * @param strategy 策略
     */
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
        log.info("切换策略: {}", strategy.getName());
    }
    
    /**
     * 执行策略
     * 
     * @param a 参数a
     * @param b 参数b
     * @return 执行结果
     */
    public int executeStrategy(int a, int b) {
        if (strategy == null) {
            throw new IllegalStateException("策略未设置");
        }
        
        log.info("使用策略 [{}] 执行计算", strategy.getName());
        return strategy.execute(a, b);
    }
}
