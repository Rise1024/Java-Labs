package com.javalaabs.patterns.structural.proxy;

/**
 * 主题接口
 * 
 * @author JavaLabs
 */
public interface Subject {
    
    /**
     * 请求方法
     * 
     * @param request 请求内容
     * @return 响应结果
     */
    String request(String request);
}
