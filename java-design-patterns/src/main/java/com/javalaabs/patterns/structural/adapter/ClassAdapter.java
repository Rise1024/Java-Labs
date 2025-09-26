package com.javalaabs.patterns.structural.adapter;

import lombok.extern.slf4j.Slf4j;

/**
 * 类适配器 - 使用继承方式
 * 
 * @author JavaLabs
 */
@Slf4j
public class ClassAdapter extends Adaptee implements Target {
    
    @Override
    public String request(String data) {
        log.info("类适配器转换请求: {}", data);
        // 直接调用父类的方法
        return specificRequest(data);
    }
}
