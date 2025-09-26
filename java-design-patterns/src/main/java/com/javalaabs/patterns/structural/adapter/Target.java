package com.javalaabs.patterns.structural.adapter;

/**
 * 目标接口 - 客户端期望的接口
 * 
 * @author JavaLabs
 */
public interface Target {
    
    /**
     * 请求方法
     * 
     * @param data 数据
     * @return 处理结果
     */
    String request(String data);
}
