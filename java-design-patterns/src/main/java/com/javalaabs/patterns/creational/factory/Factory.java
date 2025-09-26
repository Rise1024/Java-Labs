package com.javalaabs.patterns.creational.factory;

/**
 * 工厂方法模式 - 抽象工厂
 * 
 * @author JavaLabs
 */
public abstract class Factory {
    
    /**
     * 工厂方法 - 由子类实现
     * 
     * @return 产品实例
     */
    public abstract Product createProduct();
    
    /**
     * 模板方法 - 定义产品使用流程
     * 
     * @return 产品实例
     */
    public final Product getProduct() {
        Product product = createProduct();
        // 可以在这里添加通用的产品初始化逻辑
        return product;
    }
}
