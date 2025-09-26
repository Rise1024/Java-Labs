package com.javalaabs.patterns.structural.decorator;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体装饰器 - 糖装饰器
 * 
 * @author JavaLabs
 */
@Slf4j
public class SugarDecorator extends BaseDecorator {
    
    public SugarDecorator(Component component) {
        super(component);
    }
    
    @Override
    public String operation() {
        String result = super.operation() + " + 糖";
        log.info("添加糖装饰: {}", result);
        return result;
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 1.0;
    }
}
