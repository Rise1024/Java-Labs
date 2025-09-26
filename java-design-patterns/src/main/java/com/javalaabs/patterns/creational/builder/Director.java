package com.javalaabs.patterns.creational.builder;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 指挥者类 - 控制建造流程
 * 
 * @author JavaLabs
 */
@Slf4j
@AllArgsConstructor
public class Director {
    
    private ProductBuilder builder;
    
    /**
     * 构建标准产品
     * 
     * @return 产品实例
     */
    public ComplexProduct constructStandardProduct() {
        log.info("开始构建标准产品");
        
        builder.buildPartA();
        builder.buildPartB();
        builder.addFeature("标准特性1");
        builder.addFeature("标准特性2");
        
        return builder.getProduct();
    }
    
    /**
     * 构建高级产品
     * 
     * @return 产品实例
     */
    public ComplexProduct constructAdvancedProduct() {
        log.info("开始构建高级产品");
        
        builder.buildPartA();
        builder.buildPartB();
        builder.buildPartC();
        builder.addFeature("高级特性1");
        builder.addFeature("高级特性2");
        builder.addFeature("高级特性3");
        
        return builder.getProduct();
    }
}
