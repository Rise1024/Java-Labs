package com.javalaabs.patterns.behavioral.strategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体策略 - 乘法策略
 * 
 * @author JavaLabs
 */
@Slf4j
public class MultiplyStrategy implements Strategy {
    
    @Override
    public int execute(int a, int b) {
        int result = a * b;
        log.info("执行乘法策略: {} * {} = {}", a, b, result);
        return result;
    }
    
    @Override
    public String getName() {
        return "乘法策略";
    }
}
