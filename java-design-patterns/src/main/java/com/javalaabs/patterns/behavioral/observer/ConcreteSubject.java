package com.javalaabs.patterns.behavioral.observer;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 具体主题 - 新闻发布者
 * 
 * @author JavaLabs
 */
@Slf4j
public class ConcreteSubject implements Subject {
    
    private final List<Observer> observers = new ArrayList<>();
    private String state;
    
    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
        log.info("添加观察者: {}", observer.getName());
    }
    
    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        log.info("移除观察者: {}", observer.getName());
    }
    
    @Override
    public void notifyObservers(String message) {
        log.info("通知 {} 个观察者: {}", observers.size(), message);
        for (Observer observer : observers) {
            observer.update(message);
        }
    }
    
    /**
     * 设置状态并通知观察者
     * 
     * @param state 新状态
     */
    public void setState(String state) {
        this.state = state;
        log.info("主题状态改变: {}", state);
        notifyObservers(state);
    }
    
    /**
     * 获取当前状态
     * 
     * @return 当前状态
     */
    public String getState() {
        return state;
    }
}
