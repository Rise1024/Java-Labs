package com.javalaabs.patterns.creational.singleton;

import lombok.extern.slf4j.Slf4j;

/**
 * 单例模式 - 枚举实现
 * 
 * 特点：
 * - 线程安全
 * - 防止反射攻击
 * - 防止序列化攻击
 * - 代码简洁
 * 
 * 推荐使用这种方式实现单例模式
 * 
 * @author JavaLabs
 */
public enum EnumSingleton {
    
    INSTANCE;
    
    // 构造函数
    EnumSingleton() {
        System.out.println("创建枚举单例实例：INSTANCE");
    }
    
    /**
     * 业务方法示例
     * 
     * @param message 消息
     */
    public void doSomething(String message) {
        System.out.println("枚举单例实例 " + this.hashCode() + " 执行业务逻辑：" + message);
    }
    
    /**
     * 获取实例的静态方法（可选）
     * 
     * @return 单例实例
     */
    public static EnumSingleton getInstance() {
        return EnumSingleton.INSTANCE;
    }
}
