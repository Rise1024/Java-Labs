package com.javalaabs.patterns.behavioral.observer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 具体观察者 - 新闻订阅者
 * 
 * @author JavaLabs
 */
@Slf4j
@AllArgsConstructor
@Getter
public class ConcreteObserver implements Observer {
    
    private final String name;
    
    @Override
    public void update(String message) {
        log.info("观察者 [{}] 收到更新: {}", name, message);
        // 可以在这里添加具体的处理逻辑
        processMessage(message);
    }
    
    /**
     * 处理消息
     * 
     * @param message 消息
     */
    private void processMessage(String message) {
        log.info("观察者 [{}] 处理消息: {}", name, message);
    }
}
