package com.javalaabs.patterns.creational.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体工厂A - 创建产品A
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteFactoryA extends Factory {
    
    @Override
    public Product createProduct() {
        log.info("工厂A创建产品A");
        return new ConcreteProductA();
    }
}
