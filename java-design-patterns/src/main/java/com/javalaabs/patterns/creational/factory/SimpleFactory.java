package com.javalaabs.patterns.creational.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * 简单工厂模式
 * 
 * 特点：
 * - 一个工厂类负责创建所有产品
 * - 客户端无需知道具体产品类
 * - 违反开闭原则（新增产品需要修改工厂类）
 * 
 * @author JavaLabs
 */
@Slf4j
public class SimpleFactory {
    
    /**
     * 产品类型枚举
     */
    public enum ProductType {
        PRODUCT_A, PRODUCT_B
    }
    
    /**
     * 创建产品
     * 
     * @param type 产品类型
     * @return 产品实例
     */
    public static Product createProduct(ProductType type) {
        log.info("简单工厂创建产品：{}", type);
        
        return switch (type) {
            case PRODUCT_A -> new ConcreteProductA();
            case PRODUCT_B -> new ConcreteProductB();
        };
    }
    
    /**
     * 创建产品（字符串参数）
     * 
     * @param type 产品类型字符串
     * @return 产品实例
     * @throws IllegalArgumentException 不支持的产品类型
     */
    public static Product createProduct(String type) {
        log.info("简单工厂创建产品：{}", type);
        
        return switch (type.toUpperCase()) {
            case "A", "PRODUCT_A" -> new ConcreteProductA();
            case "B", "PRODUCT_B" -> new ConcreteProductB();
            default -> throw new IllegalArgumentException("不支持的产品类型: " + type);
        };
    }
}
