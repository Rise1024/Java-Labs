package com.javalaabs.patterns.creational.builder;

/**
 * 抽象建造者接口
 * 
 * @author JavaLabs
 */
public interface ProductBuilder {
    
    /**
     * 建造零件A
     */
    void buildPartA();
    
    /**
     * 建造零件B
     */
    void buildPartB();
    
    /**
     * 建造零件C
     */
    void buildPartC();
    
    /**
     * 添加特性
     * 
     * @param feature 特性
     */
    void addFeature(String feature);
    
    /**
     * 获取产品
     * 
     * @return 产品实例
     */
    ComplexProduct getProduct();
}
