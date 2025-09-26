package com.javalaabs.patterns.creational.builder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * 复杂产品类 - 演示传统建造者模式
 * 
 * @author JavaLabs
 */
@Getter
@Setter
@ToString
public class ComplexProduct {
    
    private String partA;
    private String partB;
    private String partC;
    private List<String> features = new ArrayList<>();
    
    /**
     * 添加特性
     * 
     * @param feature 特性
     */
    public void addFeature(String feature) {
        features.add(feature);
    }
}
