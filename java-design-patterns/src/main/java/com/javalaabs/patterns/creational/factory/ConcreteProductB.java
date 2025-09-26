package com.javalaabs.patterns.creational.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体产品B
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteProductB implements Product {
    
    @Override
    public void operation() {
        log.info("执行产品B的操作");
    }
    
    @Override
    public String getType() {
        return "ProductB";
    }
}
