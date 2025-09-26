package com.javalaabs.patterns.creational.builder;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体建造者实现
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteProductBuilder implements ProductBuilder {
    
    private final ComplexProduct product = new ComplexProduct();
    
    @Override
    public void buildPartA() {
        product.setPartA("高级零件A");
        log.info("建造零件A: {}", product.getPartA());
    }
    
    @Override
    public void buildPartB() {
        product.setPartB("高级零件B");
        log.info("建造零件B: {}", product.getPartB());
    }
    
    @Override
    public void buildPartC() {
        product.setPartC("高级零件C");
        log.info("建造零件C: {}", product.getPartC());
    }
    
    @Override
    public void addFeature(String feature) {
        product.addFeature(feature);
        log.info("添加特性: {}", feature);
    }
    
    @Override
    public ComplexProduct getProduct() {
        log.info("获取建造完成的产品: {}", product);
        return product;
    }
}
