package com.javalaabs.patterns.structural.decorator;

/**
 * 组件接口
 * 
 * @author JavaLabs
 */
public interface Component {
    
    /**
     * 操作方法
     * 
     * @return 操作结果
     */
    String operation();
    
    /**
     * 获取成本
     * 
     * @return 成本
     */
    double getCost();
}
