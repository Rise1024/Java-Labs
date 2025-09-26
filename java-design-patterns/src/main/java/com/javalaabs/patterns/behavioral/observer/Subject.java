package com.javalaabs.patterns.behavioral.observer;

/**
 * 主题接口
 * 
 * @author JavaLabs
 */
public interface Subject {
    
    /**
     * 添加观察者
     * 
     * @param observer 观察者
     */
    void addObserver(Observer observer);
    
    /**
     * 移除观察者
     * 
     * @param observer 观察者
     */
    void removeObserver(Observer observer);
    
    /**
     * 通知所有观察者
     * 
     * @param message 消息
     */
    void notifyObservers(String message);
}
