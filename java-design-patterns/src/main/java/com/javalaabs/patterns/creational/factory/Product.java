package com.javalaabs.patterns.creational.factory;

/**
 * 产品接口
 * 
 * @author JavaLabs
 */
public interface Product {
    
    /**
     * 产品操作
     */
    void operation();
    
    /**
     * 获取产品类型
     * 
     * @return 产品类型
     */
    String getType();
}
