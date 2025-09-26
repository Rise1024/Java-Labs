package com.javalaabs.patterns.behavioral.strategy;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体策略 - 加法策略
 * 
 * @author JavaLabs
 */
@Slf4j
public class AddStrategy implements Strategy {
    
    @Override
    public int execute(int a, int b) {
        int result = a + b;
        log.info("执行加法策略: {} + {} = {}", a, b, result);
        return result;
    }
    
    @Override
    public String getName() {
        return "加法策略";
    }
}
