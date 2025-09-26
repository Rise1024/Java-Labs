package com.javalaabs.patterns.structural.decorator;

import lombok.AllArgsConstructor;

/**
 * 抽象装饰器
 * 
 * @author JavaLabs
 */
@AllArgsConstructor
public abstract class BaseDecorator implements Component {
    
    protected final Component component;
    
    @Override
    public String operation() {
        return component.operation();
    }
    
    @Override
    public double getCost() {
        return component.getCost();
    }
}
