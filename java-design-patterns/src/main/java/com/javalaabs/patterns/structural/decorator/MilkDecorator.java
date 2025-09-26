package com.javalaabs.patterns.structural.decorator;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体装饰器 - 牛奶装饰器
 * 
 * @author JavaLabs
 */
@Slf4j
public class MilkDecorator extends BaseDecorator {
    
    public MilkDecorator(Component component) {
        super(component);
    }
    
    @Override
    public String operation() {
        String result = super.operation() + " + 牛奶";
        log.info("添加牛奶装饰: {}", result);
        return result;
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 2.0;
    }
}
