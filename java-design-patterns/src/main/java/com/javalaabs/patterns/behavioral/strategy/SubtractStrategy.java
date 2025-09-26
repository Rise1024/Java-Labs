package com.javalaabs.patterns.behavioral.strategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体策略 - 减法策略
 * 
 * @author JavaLabs
 */
@Slf4j
public class SubtractStrategy implements Strategy {
    
    @Override
    public int execute(int a, int b) {
        int result = a - b;
        log.info("执行减法策略: {} - {} = {}", a, b, result);
        return result;
    }
    
    @Override
    public String getName() {
        return "减法策略";
    }
}
