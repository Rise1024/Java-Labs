package com.javalaabs.patterns.structural.adapter;

import lombok.extern.slf4j.Slf4j;

/**
 * 被适配者 - 需要适配的现有类
 * 
 * @author JavaLabs
 */
@Slf4j
public class Adaptee {
    
    /**
     * 特殊请求方法 - 与Target接口不兼容
     * 
     * @param input 输入数据
     * @return 处理结果
     */
    public String specificRequest(String input) {
        String result = "Adaptee处理: " + input.toUpperCase();
        log.info("被适配者处理请求: {} -> {}", input, result);
        return result;
    }
}
