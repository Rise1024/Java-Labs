package com.javalaabs.patterns.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * 设计模式通用工具类
 * 
 * @author JavaLabs
 */
@Slf4j
@UtilityClass
public class PatternUtils {
    
    /**
     * 打印分隔线
     */
    public static void printSeparator() {
        log.info("=====================================");
    }
    
    /**
     * 打印模式标题
     * 
     * @param patternName 模式名称
     */
    public static void printPatternTitle(String patternName) {
        printSeparator();
        log.info("演示 {} 设计模式", patternName);
        printSeparator();
    }
    
    /**
     * 打印步骤信息
     * 
     * @param step 步骤描述
     */
    public static void printStep(String step) {
        log.info(">>> {}", step);
    }
    
    /**
     * 打印对象信息
     * 
     * @param obj 对象
     * @param description 描述
     */
    public static void printObject(Object obj, String description) {
        log.info("{}: {} (HashCode: {})", description, obj, 
                obj != null ? obj.hashCode() : "null");
    }
    
    /**
     * 模拟延迟操作
     * 
     * @param milliseconds 延迟毫秒数
     */
    public static void delay(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("延迟操作被中断", e);
        }
    }
}
