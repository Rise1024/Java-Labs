package com.javalaabs.patterns.structural.adapter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 对象适配器 - 使用组合方式
 * 
 * @author JavaLabs
 */
@Slf4j
@AllArgsConstructor
public class ObjectAdapter implements Target {
    
    private final Adaptee adaptee;
    
    @Override
    public String request(String data) {
        log.info("对象适配器转换请求: {}", data);
        // 将Target接口的调用转换为Adaptee的调用
        return adaptee.specificRequest(data);
    }
}
