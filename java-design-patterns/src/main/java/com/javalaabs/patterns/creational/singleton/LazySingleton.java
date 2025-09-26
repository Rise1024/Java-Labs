package com.javalaabs.patterns.creational.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * 单例模式 - 懒汉式实现（双重检查锁定）
 * 
 * 特点：
 * - 延迟加载
 * - 线程安全
 * - 性能较好
 * 
 * @author JavaLabs
 */
@Slf4j
public class LazySingleton {
    
    // volatile关键字确保多线程环境下的可见性
    private static volatile LazySingleton instance;
    
    // 私有构造函数
    private LazySingleton() {
        log.info("创建懒汉式单例实例：{}", this.hashCode());
    }
    
    /**
     * 获取单例实例 - 双重检查锁定
     * 
     * @return 单例实例
     */
    public static LazySingleton getInstance() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        log.debug("获取懒汉式单例实例：{}", instance.hashCode());
        return instance;
    }
    
    /**
     * 业务方法示例
     * 
     * @param message 消息
     */
    public void doSomething(String message) {
        log.info("懒汉式单例实例 {} 执行业务逻辑：{}", this.hashCode(), message);
    }
}
