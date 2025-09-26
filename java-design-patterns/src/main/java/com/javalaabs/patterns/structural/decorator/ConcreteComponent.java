package com.javalaabs.patterns.structural.decorator;

import lombok.extern.slf4j.Slf4j;

/**
 * 具体组件 - 基础咖啡
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteComponent implements Component {
    
    @Override
    public String operation() {
        String result = "基础咖啡";
        log.info("制作: {}", result);
        return result;
    }
    
    @Override
    public double getCost() {
        return 10.0;
    }
}
