package com.javalaabs.patterns.creational.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * 单例模式 - 饿汉式实现
 * 
 * 特点：
 * - 类加载时即创建实例
 * - 线程安全
 * - 无延迟加载
 * 
 * @author JavaLabs
 */
@Slf4j
public class Singleton {
    
    // 饿汉式：类加载时创建实例
    private static final Singleton INSTANCE = new Singleton();
    
    // 私有构造函数
    private Singleton() {
        log.info("创建单例实例：{}", this.hashCode());
    }
    
    /**
     * 获取单例实例
     * 
     * @return 单例实例
     */
    public static Singleton getInstance() {
        log.debug("获取单例实例：{}", INSTANCE.hashCode());
        return INSTANCE;
    }
    
    /**
     * 业务方法示例
     * 
     * @param message 消息
     */
    public void doSomething(String message) {
        log.info("单例实例 {} 执行业务逻辑：{}", this.hashCode(), message);
    }
}
