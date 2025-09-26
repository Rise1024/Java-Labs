package com.javalaabs.patterns.behavioral.strategy;

/**
 * 策略接口
 * 
 * @author JavaLabs
 */
public interface Strategy {
    
    /**
     * 执行策略
     * 
     * @param a 参数a
     * @param b 参数b
     * @return 计算结果
     */
    int execute(int a, int b);
    
    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    String getName();
}
