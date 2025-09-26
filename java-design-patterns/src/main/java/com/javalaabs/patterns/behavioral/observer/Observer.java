package com.javalaabs.patterns.behavioral.observer;

/**
 * 观察者接口
 * 
 * @author JavaLabs
 */
public interface Observer {
    
    /**
     * 更新方法
     * 
     * @param message 消息
     */
    void update(String message);
    
    /**
     * 获取观察者名称
     * 
     * @return 观察者名称
     */
    String getName();
}
