package com.javalaabs.patterns;

import com.javalaabs.patterns.behavioral.observer.ConcreteObserver;
import com.javalaabs.patterns.behavioral.observer.ConcreteSubject;
import com.javalaabs.patterns.behavioral.strategy.*;
import com.javalaabs.patterns.creational.builder.Computer;
import com.javalaabs.patterns.creational.builder.ConcreteProductBuilder;
import com.javalaabs.patterns.creational.builder.Director;
import com.javalaabs.patterns.creational.factory.*;
import com.javalaabs.patterns.creational.singleton.EnumSingleton;
import com.javalaabs.patterns.creational.singleton.LazySingleton;
import com.javalaabs.patterns.creational.singleton.Singleton;
import com.javalaabs.patterns.structural.adapter.Adaptee;
import com.javalaabs.patterns.structural.adapter.ObjectAdapter;
import com.javalaabs.patterns.structural.adapter.Target;
import com.javalaabs.patterns.structural.decorator.ConcreteComponent;
import com.javalaabs.patterns.structural.decorator.MilkDecorator;
import com.javalaabs.patterns.structural.decorator.SugarDecorator;
import com.javalaabs.patterns.structural.proxy.Proxy;
import com.javalaabs.patterns.utils.PatternUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 设计模式综合演示程序
 * 
 * @author JavaLabs
 */
@Slf4j
public class DesignPatternDemo {
    
    public static void main(String[] args) {
        log.info("=== 设计模式学习演示开始 ===");
        
        // 演示创建型模式
        demonstrateCreationalPatterns();
        
        // 演示结构型模式
        demonstrateStructuralPatterns();
        
        // 演示行为型模式
        demonstrateBehavioralPatterns();
        
        log.info("=== 设计模式学习演示结束 ===");
    }
    
    /**
     * 演示创建型模式
     */
    private static void demonstrateCreationalPatterns() {
        PatternUtils.printPatternTitle("创建型模式");
        
        // 单例模式演示
        demonstrateSingletonPattern();
        
        // 工厂模式演示
        demonstrateFactoryPattern();
        
        // 建造者模式演示
        demonstrateBuilderPattern();
    }
    
    /**
     * 演示单例模式
     */
    private static void demonstrateSingletonPattern() {
        PatternUtils.printStep("演示单例模式");
        
        // 饿汉式单例
        Singleton instance1 = Singleton.getInstance();
        Singleton instance2 = Singleton.getInstance();
        PatternUtils.printObject(instance1, "饿汉式单例实例1");
        PatternUtils.printObject(instance2, "饿汉式单例实例2");
        log.info("饿汉式单例相同实例: {}", instance1 == instance2);
        
        // 懒汉式单例
        LazySingleton lazyInstance1 = LazySingleton.getInstance();
        LazySingleton lazyInstance2 = LazySingleton.getInstance();
        PatternUtils.printObject(lazyInstance1, "懒汉式单例实例1");
        PatternUtils.printObject(lazyInstance2, "懒汉式单例实例2");
        log.info("懒汉式单例相同实例: {}", lazyInstance1 == lazyInstance2);
        
        // 枚举单例
        EnumSingleton enumInstance1 = EnumSingleton.getInstance();
        EnumSingleton enumInstance2 = EnumSingleton.INSTANCE;
        PatternUtils.printObject(enumInstance1, "枚举单例实例1");
        PatternUtils.printObject(enumInstance2, "枚举单例实例2");
        log.info("枚举单例相同实例: {}", enumInstance1 == enumInstance2);
        
        log.info("");
    }
    
    /**
     * 演示工厂模式
     */
    private static void demonstrateFactoryPattern() {
        PatternUtils.printStep("演示工厂模式");
        
        // 简单工厂模式
        log.info("--- 简单工厂模式 ---");
        Product productA = SimpleFactory.createProduct(SimpleFactory.ProductType.PRODUCT_A);
        Product productB = SimpleFactory.createProduct("B");
        productA.operation();
        productB.operation();
        
        // 工厂方法模式
        log.info("--- 工厂方法模式 ---");
        Factory factoryA = new ConcreteFactoryA();
        Factory factoryB = new ConcreteFactoryB();
        
        Product productFromA = factoryA.getProduct();
        Product productFromB = factoryB.getProduct();
        
        productFromA.operation();
        productFromB.operation();
        
        log.info("");
    }
    
    /**
     * 演示建造者模式
     */
    private static void demonstrateBuilderPattern() {
        PatternUtils.printStep("演示建造者模式");
        
        // 现代建造者模式（静态内部类）
        log.info("--- 现代建造者模式 ---");
        Computer computer = new Computer.Builder("Intel i7", "16GB DDR4")
                .storage("1TB SSD")
                .graphics("NVIDIA GTX 3080")
                .wifi(true)
                .bluetooth(true)
                .build();
        
        log.info("建造的计算机: {}", computer);
        
        // 传统建造者模式（Director + Builder）
        log.info("--- 传统建造者模式 ---");
        ConcreteProductBuilder builder = new ConcreteProductBuilder();
        Director director = new Director(builder);
        
        var standardProduct = director.constructStandardProduct();
        log.info("标准产品: {}", standardProduct);
        
        var advancedProduct = director.constructAdvancedProduct();
        log.info("高级产品: {}", advancedProduct);
        
        log.info("");
    }
    
    /**
     * 演示结构型模式
     */
    private static void demonstrateStructuralPatterns() {
        PatternUtils.printPatternTitle("结构型模式");
        
        // 适配器模式演示
        demonstrateAdapterPattern();
        
        // 装饰器模式演示
        demonstrateDecoratorPattern();
        
        // 代理模式演示
        demonstrateProxyPattern();
    }
    
    /**
     * 演示适配器模式
     */
    private static void demonstrateAdapterPattern() {
        PatternUtils.printStep("演示适配器模式");
        
        Adaptee adaptee = new Adaptee();
        Target adapter = new ObjectAdapter(adaptee);
        
        String result = adapter.request("测试数据");
        log.info("适配器返回结果: {}", result);
        
        log.info("");
    }
    
    /**
     * 演示装饰器模式
     */
    private static void demonstrateDecoratorPattern() {
        PatternUtils.printStep("演示装饰器模式");
        
        // 基础组件
        var coffee = new ConcreteComponent();
        log.info("基础咖啡: {} - 价格: {}", coffee.operation(), coffee.getCost());
        
        // 添加牛奶装饰
        var milkCoffee = new MilkDecorator(coffee);
        log.info("牛奶咖啡: {} - 价格: {}", milkCoffee.operation(), milkCoffee.getCost());
        
        // 添加糖装饰
        var sweetMilkCoffee = new SugarDecorator(milkCoffee);
        log.info("甜牛奶咖啡: {} - 价格: {}", sweetMilkCoffee.operation(), sweetMilkCoffee.getCost());
        
        log.info("");
    }
    
    /**
     * 演示代理模式
     */
    private static void demonstrateProxyPattern() {
        PatternUtils.printStep("演示代理模式");
        
        Proxy proxy = new Proxy();
        
        // 正常请求
        String result1 = proxy.request("正常请求");
        log.info("第一次请求结果: {}", result1);
        
        // 缓存请求
        String result2 = proxy.request("正常请求");
        log.info("第二次请求结果（缓存）: {}", result2);
        
        // 被拒绝的请求
        String result3 = proxy.request("forbidden请求");
        log.info("禁止请求结果: {}", result3);
        
        log.info("");
    }
    
    /**
     * 演示行为型模式
     */
    private static void demonstrateBehavioralPatterns() {
        PatternUtils.printPatternTitle("行为型模式");
        
        // 观察者模式演示
        demonstrateObserverPattern();
        
        // 策略模式演示
        demonstrateStrategyPattern();
    }
    
    /**
     * 演示观察者模式
     */
    private static void demonstrateObserverPattern() {
        PatternUtils.printStep("演示观察者模式");
        
        ConcreteSubject subject = new ConcreteSubject();
        
        // 创建观察者
        ConcreteObserver observer1 = new ConcreteObserver("订阅者1");
        ConcreteObserver observer2 = new ConcreteObserver("订阅者2");
        ConcreteObserver observer3 = new ConcreteObserver("订阅者3");
        
        // 添加观察者
        subject.addObserver(observer1);
        subject.addObserver(observer2);
        subject.addObserver(observer3);
        
        // 发布消息
        subject.setState("重要新闻：Java 设计模式学习完成！");
        
        // 移除一个观察者
        subject.removeObserver(observer2);
        
        // 再次发布消息
        subject.setState("追加新闻：开始学习 Spring Framework！");
        
        log.info("");
    }
    
    /**
     * 演示策略模式
     */
    private static void demonstrateStrategyPattern() {
        PatternUtils.printStep("演示策略模式");
        
        Context context = new Context(new AddStrategy());
        
        // 使用加法策略
        int result1 = context.executeStrategy(10, 5);
        log.info("计算结果: {}", result1);
        
        // 切换到减法策略
        context.setStrategy(new SubtractStrategy());
        int result2 = context.executeStrategy(10, 5);
        log.info("计算结果: {}", result2);
        
        // 切换到乘法策略
        context.setStrategy(new MultiplyStrategy());
        int result3 = context.executeStrategy(10, 5);
        log.info("计算结果: {}", result3);
        
        log.info("");
    }
}
