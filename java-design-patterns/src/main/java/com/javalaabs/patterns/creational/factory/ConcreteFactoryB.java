package com.javalaabs.patterns.creational.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体工厂B - 创建产品B
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteFactoryB extends Factory {
    
    @Override
    public Product createProduct() {
        log.info("工厂B创建产品B");
        return new ConcreteProductB();
    }
}
