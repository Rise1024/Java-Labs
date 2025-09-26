package com.javalaabs.patterns.creational.factory;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体产品A
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteProductA implements Product {
    
    @Override
    public void operation() {
        log.info("执行产品A的操作");
    }
    
    @Override
    public String getType() {
        return "ProductA";
    }
}
