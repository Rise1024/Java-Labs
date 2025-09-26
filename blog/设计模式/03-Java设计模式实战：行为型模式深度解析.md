---
title: Java设计模式实战：行为型模式深度解析
description: 深入解析Java行为型设计模式，包括观察者、策略、命令等模式的实现原理和最佳实践
tags: [Java, 设计模式, 行为型模式, 观察者模式, 策略模式, 命令模式]
category: 设计模式
date: 2025-09-26
---

# Java设计模式实战：行为型模式深度解析

## 🎯 引言

行为型模式关注对象之间的通信，描述对象之间如何交互和如何分配职责。这些模式不仅关注对象的结构，更关注对象之间的交互流程和算法的分配。

本文将深入解析最重要的行为型设计模式，结合现代Java特性和企业级开发实践，提供可直接应用的解决方案。

## 📚 行为型模式概述

行为型模式主要解决以下问题：
- **算法变化** - 封装算法，使算法可以独立于使用它的客户端变化
- **对象交互** - 定义对象间的交互方式和职责分配
- **状态管理** - 管理对象状态变化和状态转换
- **请求处理** - 规范请求的发送和处理流程

### 行为型模式分类

| 模式名称 | 主要目的 | 适用场景 | 复杂度 |
|---------|---------|---------|-------|
| **观察者模式** | 一对多依赖关系 | 事件通知、MVC架构 | ⭐⭐⭐ |
| **策略模式** | 算法族封装 | 算法变化、支付方式选择 | ⭐⭐⭐ |
| **命令模式** | 请求封装 | 撤销操作、队列处理 | ⭐⭐⭐⭐ |
| **模板方法模式** | 算法骨架定义 | 框架设计、流程控制 | ⭐⭐⭐ |
| **责任链模式** | 请求传递链 | 过滤器、中间件 | ⭐⭐⭐⭐ |
| **状态模式** | 状态转换 | 状态机、工作流 | ⭐⭐⭐⭐⭐ |
| **访问者模式** | 操作与结构分离 | 编译器、AST处理 | ⭐⭐⭐⭐⭐ |
| **中介者模式** | 对象间交互 | 组件通信、聊天室 | ⭐⭐⭐⭐ |
| **备忘录模式** | 状态保存恢复 | 撤销功能、快照 | ⭐⭐⭐ |
| **迭代器模式** | 集合遍历 | 数据结构遍历 | ⭐⭐ |
| **解释器模式** | 语言解释 | DSL、表达式解析 | ⭐⭐⭐⭐⭐ |

## 👀 观察者模式详解

### 模式定义

定义对象间的一种一对多的依赖关系，当一个对象的状态发生改变时，所有依赖于它的对象都得到通知并被自动更新。

### 传统实现

```java
// 观察者接口
public interface Observer {
    void update(String message);
    String getName();
}

// 主题接口
public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String message);
}

// 具体主题 - 新闻发布者
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
    
    public void setState(String state) {
        this.state = state;
        log.info("主题状态改变: {}", state);
        notifyObservers(state);
    }
}

// 具体观察者
@Slf4j
@AllArgsConstructor
@Getter
public class ConcreteObserver implements Observer {
    private final String name;
    
    @Override
    public void update(String message) {
        log.info("观察者 [{}] 收到更新: {}", name, message);
        processMessage(message);
    }
    
    private void processMessage(String message) {
        log.info("观察者 [{}] 处理消息: {}", name, message);
    }
}
```

### 现代Java实现 - 使用函数式接口

```java
// 使用Consumer接口简化观察者
@Slf4j
public class ModernObservable {
    private final List<Consumer<String>> observers = new CopyOnWriteArrayList<>();
    
    public void subscribe(Consumer<String> observer) {
        observers.add(observer);
        log.info("添加观察者，当前总数: {}", observers.size());
    }
    
    public void unsubscribe(Consumer<String> observer) {
        observers.remove(observer);
        log.info("移除观察者，当前总数: {}", observers.size());
    }
    
    public void publish(String message) {
        log.info("发布消息: {}", message);
        observers.forEach(observer -> {
            try {
                observer.accept(message);
            } catch (Exception e) {
                log.error("观察者处理消息时发生错误", e);
            }
        });
    }
}

// 使用示例
public class ModernObserverDemo {
    public static void main(String[] args) {
        ModernObservable observable = new ModernObservable();
        
        // 订阅观察者
        observable.subscribe(message -> 
            System.out.println("邮件通知: " + message));
        observable.subscribe(message -> 
            System.out.println("短信通知: " + message));
        observable.subscribe(message -> 
            System.out.println("推送通知: " + message));
        
        // 发布消息
        observable.publish("用户注册成功");
    }
}
```

### 企业级应用 - 事件驱动架构

```java
// 事件基类
@Getter
@AllArgsConstructor
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String eventType;
    
    public DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }
}

// 用户注册事件
@Getter
public class UserRegisteredEvent extends DomainEvent {
    private final String userId;
    private final String email;
    private final String username;
    
    public UserRegisteredEvent(String userId, String email, String username) {
        super("USER_REGISTERED");
        this.userId = userId;
        this.email = email;
        this.username = username;
    }
}

// 事件处理器接口
@FunctionalInterface
public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
}

// 事件总线
@Slf4j
@Component
public class EventBus {
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> 
        handlers = new ConcurrentHashMap<>();
    
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventType, 
                                                  EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(handler);
        log.info("注册事件处理器: {} -> {}", eventType.getSimpleName(), 
                handler.getClass().getSimpleName());
    }
    
    @SuppressWarnings("unchecked")
    public void publish(DomainEvent event) {
        log.info("发布事件: {} [{}]", event.getEventType(), event.getEventId());
        
        List<EventHandler<? extends DomainEvent>> eventHandlers = 
            handlers.get(event.getClass());
        
        if (eventHandlers != null) {
            eventHandlers.forEach(handler -> {
                try {
                    ((EventHandler<DomainEvent>) handler).handle(event);
                } catch (Exception e) {
                    log.error("事件处理失败: {}", event.getEventId(), e);
                }
            });
        }
    }
}

// 事件处理器实现
@Component
@Slf4j
public class UserEventHandlers {
    
    @EventHandler
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("处理用户注册事件: {}", event.getUserId());
        
        // 发送欢迎邮件
        sendWelcomeEmail(event.getEmail(), event.getUsername());
        
        // 创建用户配置文件
        createUserProfile(event.getUserId());
        
        // 发送统计数据
        updateRegistrationStatistics();
    }
    
    private void sendWelcomeEmail(String email, String username) {
        log.info("发送欢迎邮件到: {}", email);
    }
    
    private void createUserProfile(String userId) {
        log.info("为用户创建配置文件: {}", userId);
    }
    
    private void updateRegistrationStatistics() {
        log.info("更新注册统计数据");
    }
}
```

## 🎯 策略模式详解

### 模式定义

定义一系列算法，把它们一个个封装起来，并且使它们可相互替换。策略模式使得算法可独立于使用它的客户端而变化。

### 核心实现

```java
// 策略接口
public interface Strategy {
    int execute(int a, int b);
    String getName();
}

// 具体策略实现
@Slf4j
public class AddStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        int result = a + b;
        log.info("执行加法策略: {} + {} = {}", a, b, result);
        return result;
    }
    
    @Override
    public String getName() {
        return "加法策略";
    }
}

@Slf4j
public class MultiplyStrategy implements Strategy {
    @Override
    public int execute(int a, int b) {
        int result = a * b;
        log.info("执行乘法策略: {} * {} = {}", a, b, result);
        return result;
    }
    
    @Override
    public String getName() {
        return "乘法策略";
    }
}

// 上下文类
@Slf4j
@AllArgsConstructor
public class Context {
    private Strategy strategy;
    
    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
        log.info("切换策略: {}", strategy.getName());
    }
    
    public int executeStrategy(int a, int b) {
        if (strategy == null) {
            throw new IllegalStateException("策略未设置");
        }
        
        log.info("使用策略 [{}] 执行计算", strategy.getName());
        return strategy.execute(a, b);
    }
}
```

### 现代Java实现 - 使用枚举和函数式接口

```java
// 使用枚举实现策略模式
public enum CalculationStrategy {
    ADD((a, b) -> a + b, "加法"),
    SUBTRACT((a, b) -> a - b, "减法"),
    MULTIPLY((a, b) -> a * b, "乘法"),
    DIVIDE((a, b) -> {
        if (b == 0) throw new ArithmeticException("除数不能为零");
        return a / b;
    }, "除法");
    
    private final BinaryOperator<Integer> operation;
    private final String description;
    
    CalculationStrategy(BinaryOperator<Integer> operation, String description) {
        this.operation = operation;
        this.description = description;
    }
    
    public int calculate(int a, int b) {
        return operation.apply(a, b);
    }
    
    public String getDescription() {
        return description;
    }
}

// 现代计算器
@Slf4j
public class ModernCalculator {
    
    public int calculate(int a, int b, CalculationStrategy strategy) {
        log.info("使用 {} 计算: {} 和 {}", strategy.getDescription(), a, b);
        int result = strategy.calculate(a, b);
        log.info("计算结果: {}", result);
        return result;
    }
    
    // 支持动态策略选择
    public int calculate(int a, int b, String operation) {
        CalculationStrategy strategy = switch (operation.toUpperCase()) {
            case "ADD", "+" -> CalculationStrategy.ADD;
            case "SUBTRACT", "-" -> CalculationStrategy.SUBTRACT;
            case "MULTIPLY", "*" -> CalculationStrategy.MULTIPLY;
            case "DIVIDE", "/" -> CalculationStrategy.DIVIDE;
            default -> throw new IllegalArgumentException("不支持的运算: " + operation);
        };
        
        return calculate(a, b, strategy);
    }
}
```

### 企业级应用 - 支付策略

```java
// 支付结果
@Getter
@AllArgsConstructor
public class PaymentResult {
    private final boolean success;
    private final String message;
    private final String transactionId;
}

// 支付策略接口
public interface PaymentStrategy {
    PaymentResult processPayment(double amount, String currency);
    String getPaymentMethod();
    boolean isAvailable();
}

// 具体支付策略
@Slf4j
@Component
public class CreditCardPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(double amount, String currency) {
        log.info("处理信用卡支付: {} {}", amount, currency);
        
        // 模拟信用卡支付逻辑
        if (amount > 10000) {
            return new PaymentResult(false, "超过信用卡限额", null);
        }
        
        String transactionId = "CC-" + UUID.randomUUID().toString();
        log.info("信用卡支付成功，交易ID: {}", transactionId);
        
        return new PaymentResult(true, "支付成功", transactionId);
    }
    
    @Override
    public String getPaymentMethod() {
        return "信用卡";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 检查信用卡服务是否可用
    }
}

@Slf4j
@Component
public class DigitalWalletPaymentStrategy implements PaymentStrategy {
    
    @Override
    public PaymentResult processPayment(double amount, String currency) {
        log.info("处理数字钱包支付: {} {}", amount, currency);
        
        // 模拟数字钱包支付逻辑
        if (!checkBalance(amount)) {
            return new PaymentResult(false, "数字钱包余额不足", null);
        }
        
        String transactionId = "DW-" + UUID.randomUUID().toString();
        log.info("数字钱包支付成功，交易ID: {}", transactionId);
        
        return new PaymentResult(true, "支付成功", transactionId);
    }
    
    private boolean checkBalance(double amount) {
        // 检查钱包余额
        return amount <= 5000; // 模拟余额限制
    }
    
    @Override
    public String getPaymentMethod() {
        return "数字钱包";
    }
    
    @Override
    public boolean isAvailable() {
        return true; // 检查数字钱包服务是否可用
    }
}

// 支付服务
@Service
@Slf4j
public class PaymentService {
    private final Map<String, PaymentStrategy> strategies;
    
    public PaymentService(List<PaymentStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                    PaymentStrategy::getPaymentMethod,
                    Function.identity()
                ));
    }
    
    public PaymentResult processPayment(double amount, String currency, 
                                      String paymentMethod) {
        PaymentStrategy strategy = strategies.get(paymentMethod);
        
        if (strategy == null) {
            return new PaymentResult(false, 
                "不支持的支付方式: " + paymentMethod, null);
        }
        
        if (!strategy.isAvailable()) {
            return new PaymentResult(false, 
                paymentMethod + " 服务暂时不可用", null);
        }
        
        log.info("开始处理支付: {} {} 使用 {}", amount, currency, paymentMethod);
        
        try {
            return strategy.processPayment(amount, currency);
        } catch (Exception e) {
            log.error("支付处理失败", e);
            return new PaymentResult(false, "支付处理失败: " + e.getMessage(), null);
        }
    }
    
    public List<String> getAvailablePaymentMethods() {
        return strategies.values().stream()
                .filter(PaymentStrategy::isAvailable)
                .map(PaymentStrategy::getPaymentMethod)
                .collect(Collectors.toList());
    }
}
```

## 📝 命令模式详解

### 模式定义

将一个请求封装为一个对象，从而可以用不同的请求对客户进行参数化，对请求排队或记录请求日志，以及支持可撤销的操作。

### 核心实现

```java
// 命令接口
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

// 接收者 - 文本编辑器
@Slf4j
public class TextEditor {
    private StringBuilder content = new StringBuilder();
    
    public void insertText(String text) {
        content.append(text);
        log.info("插入文本: '{}', 当前内容: '{}'", text, content);
    }
    
    public void deleteText(int length) {
        if (length > content.length()) {
            length = content.length();
        }
        content.delete(content.length() - length, content.length());
        log.info("删除 {} 个字符, 当前内容: '{}'", length, content);
    }
    
    public String getContent() {
        return content.toString();
    }
    
    public void setContent(String content) {
        this.content = new StringBuilder(content);
        log.info("设置内容: '{}'", content);
    }
}

// 具体命令 - 插入文本命令
@AllArgsConstructor
public class InsertTextCommand implements Command {
    private final TextEditor editor;
    private final String text;
    
    @Override
    public void execute() {
        editor.insertText(text);
    }
    
    @Override
    public void undo() {
        editor.deleteText(text.length());
    }
    
    @Override
    public String getDescription() {
        return "插入文本: " + text;
    }
}

// 具体命令 - 删除文本命令
@AllArgsConstructor
public class DeleteTextCommand implements Command {
    private final TextEditor editor;
    private final int length;
    private String deletedText = "";
    
    @Override
    public void execute() {
        String content = editor.getContent();
        int deleteStart = Math.max(0, content.length() - length);
        deletedText = content.substring(deleteStart);
        editor.deleteText(length);
    }
    
    @Override
    public void undo() {
        editor.insertText(deletedText);
    }
    
    @Override
    public String getDescription() {
        return "删除 " + length + " 个字符";
    }
}

// 调用者 - 编辑器控制器
@Slf4j
public class EditorController {
    private final Stack<Command> history = new Stack<>();
    private final Stack<Command> redoStack = new Stack<>();
    
    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear(); // 执行新命令时清空重做栈
        log.info("执行命令: {}", command.getDescription());
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
            redoStack.push(command);
            log.info("撤销命令: {}", command.getDescription());
        } else {
            log.info("没有可撤销的命令");
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            history.push(command);
            log.info("重做命令: {}", command.getDescription());
        } else {
            log.info("没有可重做的命令");
        }
    }
    
    public void showHistory() {
        log.info("命令历史 ({} 个命令):", history.size());
        for (int i = 0; i < history.size(); i++) {
            log.info("  {}: {}", i + 1, history.get(i).getDescription());
        }
    }
}
```

### 企业级应用 - 任务调度系统

```java
// 异步命令接口
public interface AsyncCommand extends Command {
    CompletableFuture<Void> executeAsync();
    boolean isRetryable();
    int getMaxRetries();
}

// 数据库操作命令
@AllArgsConstructor
@Slf4j
public class DatabaseOperationCommand implements AsyncCommand {
    private final String sql;
    private final Object[] parameters;
    private final DataSource dataSource;
    private String rollbackSql;
    
    @Override
    public void execute() {
        executeSync();
    }
    
    @Override
    public CompletableFuture<Void> executeAsync() {
        return CompletableFuture.runAsync(this::executeSync);
    }
    
    private void executeSync() {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < parameters.length; i++) {
                stmt.setObject(i + 1, parameters[i]);
            }
            
            int affected = stmt.executeUpdate();
            log.info("数据库操作成功，影响 {} 行", affected);
            
        } catch (SQLException e) {
            log.error("数据库操作失败: {}", sql, e);
            throw new RuntimeException("数据库操作失败", e);
        }
    }
    
    @Override
    public void undo() {
        if (rollbackSql != null) {
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(rollbackSql)) {
                
                stmt.executeUpdate();
                log.info("回滚操作成功");
                
            } catch (SQLException e) {
                log.error("回滚操作失败: {}", rollbackSql, e);
                throw new RuntimeException("回滚操作失败", e);
            }
        }
    }
    
    @Override
    public String getDescription() {
        return "数据库操作: " + sql;
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    @Override
    public int getMaxRetries() {
        return 3;
    }
}

// 异步命令执行器
@Component
@Slf4j
public class AsyncCommandExecutor {
    private final ExecutorService executorService;
    private final Map<String, CompletableFuture<Void>> runningTasks;
    
    public AsyncCommandExecutor() {
        this.executorService = Executors.newFixedThreadPool(10);
        this.runningTasks = new ConcurrentHashMap<>();
    }
    
    public CompletableFuture<Void> executeAsync(String taskId, AsyncCommand command) {
        log.info("开始异步执行命令: {} - {}", taskId, command.getDescription());
        
        CompletableFuture<Void> future = command.executeAsync()
                .handle((result, throwable) -> {
                    if (throwable != null) {
                        return handleCommandFailure(command, throwable);
                    }
                    return result;
                })
                .whenComplete((result, throwable) -> {
                    runningTasks.remove(taskId);
                    if (throwable == null) {
                        log.info("命令执行成功: {}", taskId);
                    } else {
                        log.error("命令执行失败: {}", taskId, throwable);
                    }
                });
        
        runningTasks.put(taskId, future);
        return future;
    }
    
    private Void handleCommandFailure(AsyncCommand command, Throwable throwable) {
        if (command.isRetryable()) {
            return retryCommand(command, throwable);
        } else {
            log.error("命令执行失败且不可重试: {}", command.getDescription(), throwable);
            throw new RuntimeException(throwable);
        }
    }
    
    private Void retryCommand(AsyncCommand command, Throwable lastError) {
        for (int retry = 1; retry <= command.getMaxRetries(); retry++) {
            try {
                log.info("重试命令 (第 {} 次): {}", retry, command.getDescription());
                Thread.sleep(1000 * retry); // 指数退避
                command.execute();
                return null;
            } catch (Exception e) {
                log.warn("命令重试失败 (第 {} 次): {}", retry, command.getDescription(), e);
                lastError = e;
            }
        }
        
        log.error("命令重试次数用尽: {}", command.getDescription(), lastError);
        throw new RuntimeException("命令执行失败", lastError);
    }
    
    public void cancelTask(String taskId) {
        CompletableFuture<Void> task = runningTasks.get(taskId);
        if (task != null) {
            task.cancel(true);
            runningTasks.remove(taskId);
            log.info("任务已取消: {}", taskId);
        }
    }
    
    public List<String> getRunningTasks() {
        return new ArrayList<>(runningTasks.keySet());
    }
    
    @PreDestroy
    public void shutdown() {
        log.info("关闭异步命令执行器");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

## 🔄 其他重要行为型模式

### 模板方法模式

定义一个操作中算法的骨架，而将一些步骤延迟到子类中。

```java
// 抽象模板类
@Slf4j
public abstract class DataProcessor {
    
    // 模板方法 - 定义算法骨架
    public final void processData(String data) {
        log.info("开始数据处理流程");
        
        String validatedData = validateData(data);
        String transformedData = transformData(validatedData);
        String processedData = processBusinessLogic(transformedData);
        saveData(processedData);
        
        log.info("数据处理流程完成");
    }
    
    // 具体方法 - 通用的数据验证
    private String validateData(String data) {
        log.info("验证数据: {}", data);
        if (data == null || data.trim().isEmpty()) {
            throw new IllegalArgumentException("数据不能为空");
        }
        return data.trim();
    }
    
    // 抽象方法 - 由子类实现
    protected abstract String transformData(String data);
    protected abstract String processBusinessLogic(String data);
    
    // 钩子方法 - 子类可以选择性重写
    protected void saveData(String data) {
        log.info("保存数据到默认存储: {}", data);
    }
}

// 具体实现 - JSON数据处理器
@Slf4j
public class JsonDataProcessor extends DataProcessor {
    
    @Override
    protected String transformData(String data) {
        log.info("JSON数据转换: {}", data);
        // JSON特定的转换逻辑
        return "{\"processed\": \"" + data + "\"}";
    }
    
    @Override
    protected String processBusinessLogic(String data) {
        log.info("JSON业务逻辑处理: {}", data);
        // JSON特定的业务逻辑
        return data.replace("processed", "completed");
    }
    
    @Override
    protected void saveData(String data) {
        log.info("保存JSON数据到MongoDB: {}", data);
    }
}

// 具体实现 - XML数据处理器
@Slf4j
public class XmlDataProcessor extends DataProcessor {
    
    @Override
    protected String transformData(String data) {
        log.info("XML数据转换: {}", data);
        return "<processed>" + data + "</processed>";
    }
    
    @Override
    protected String processBusinessLogic(String data) {
        log.info("XML业务逻辑处理: {}", data);
        return data.replace("processed", "completed");
    }
    
    @Override
    protected void saveData(String data) {
        log.info("保存XML数据到关系数据库: {}", data);
    }
}
```

### 责任链模式

避免请求发送者与接收者耦合在一起，让多个对象都有可能接收请求，将这些对象连接成一条链，并且沿着这条链传递请求，直到有对象处理它为止。

```java
// 抽象处理器
public abstract class RequestHandler {
    protected RequestHandler nextHandler;
    
    public void setNext(RequestHandler handler) {
        this.nextHandler = handler;
    }
    
    public abstract void handleRequest(Request request);
    
    protected void passToNext(Request request) {
        if (nextHandler != null) {
            nextHandler.handleRequest(request);
        } else {
            throw new RuntimeException("没有处理器能够处理请求: " + request.getType());
        }
    }
}

// 请求类
@Getter
@AllArgsConstructor
public class Request {
    private final String type;
    private final String content;
    private final Map<String, Object> attributes;
}

// 具体处理器 - 认证处理器
@Slf4j
public class AuthenticationHandler extends RequestHandler {
    
    @Override
    public void handleRequest(Request request) {
        if ("AUTH".equals(request.getType())) {
            log.info("认证处理器处理请求: {}", request.getContent());
            // 认证逻辑
            if (authenticate(request)) {
                log.info("认证成功");
            } else {
                throw new RuntimeException("认证失败");
            }
        } else {
            passToNext(request);
        }
    }
    
    private boolean authenticate(Request request) {
        // 模拟认证逻辑
        return request.getAttributes().containsKey("token");
    }
}

// 具体处理器 - 授权处理器
@Slf4j
public class AuthorizationHandler extends RequestHandler {
    
    @Override
    public void handleRequest(Request request) {
        if ("AUTHZ".equals(request.getType())) {
            log.info("授权处理器处理请求: {}", request.getContent());
            // 授权逻辑
            if (authorize(request)) {
                log.info("授权成功");
            } else {
                throw new RuntimeException("授权失败");
            }
        } else {
            passToNext(request);
        }
    }
    
    private boolean authorize(Request request) {
        // 模拟授权逻辑
        return request.getAttributes().containsKey("role");
    }
}

// 责任链构建器
public class HandlerChainBuilder {
    private RequestHandler first;
    private RequestHandler current;
    
    public HandlerChainBuilder addHandler(RequestHandler handler) {
        if (first == null) {
            first = handler;
            current = handler;
        } else {
            current.setNext(handler);
            current = handler;
        }
        return this;
    }
    
    public RequestHandler build() {
        return first;
    }
}

// 使用示例
public class ChainOfResponsibilityDemo {
    public static void main(String[] args) {
        // 构建责任链
        RequestHandler chain = new HandlerChainBuilder()
                .addHandler(new AuthenticationHandler())
                .addHandler(new AuthorizationHandler())
                .build();
        
        // 创建请求
        Map<String, Object> attributes = Map.of(
                "token", "abc123",
                "role", "user"
        );
        
        Request authRequest = new Request("AUTH", "用户登录", attributes);
        Request authzRequest = new Request("AUTHZ", "访问资源", attributes);
        
        // 处理请求
        chain.handleRequest(authRequest);
        chain.handleRequest(authzRequest);
    }
}
```

## 🚀 现代Java特性在行为型模式中的应用

### 使用Stream API增强观察者模式

```java
@Slf4j
public class StreamObservable<T> {
    private final List<Consumer<T>> observers = new CopyOnWriteArrayList<>();
    
    public void subscribe(Consumer<T> observer) {
        observers.add(observer);
    }
    
    public void publish(T event) {
        observers.parallelStream()
                .forEach(observer -> {
                    try {
                        observer.accept(event);
                    } catch (Exception e) {
                        log.error("观察者处理事件失败", e);
                    }
                });
    }
    
    // 支持条件过滤的发布
    public void publishIf(T event, Predicate<T> condition) {
        if (condition.test(event)) {
            publish(event);
        }
    }
    
    // 支持事件转换
    public <R> void publishMapped(T event, Function<T, R> mapper, 
                                  Consumer<R> observer) {
        try {
            R mappedEvent = mapper.apply(event);
            observer.accept(mappedEvent);
        } catch (Exception e) {
            log.error("事件转换失败", e);
        }
    }
}
```

### 使用Optional增强责任链模式

```java
// 函数式责任链
@FunctionalInterface
public interface HandlerFunction<T> {
    Optional<T> handle(T request);
}

public class FunctionalChain<T> {
    private final List<HandlerFunction<T>> handlers = new ArrayList<>();
    
    public FunctionalChain<T> addHandler(HandlerFunction<T> handler) {
        handlers.add(handler);
        return this;
    }
    
    public Optional<T> process(T request) {
        return handlers.stream()
                .map(handler -> handler.handle(request))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}

// 使用示例
HandlerFunction<String> lengthHandler = request -> 
    request.length() > 10 ? Optional.of("长文本处理: " + request) : Optional.empty();

HandlerFunction<String> numberHandler = request -> 
    request.matches("\\d+") ? Optional.of("数字处理: " + request) : Optional.empty();

HandlerFunction<String> defaultHandler = request -> 
    Optional.of("默认处理: " + request);

FunctionalChain<String> chain = new FunctionalChain<String>()
        .addHandler(lengthHandler)
        .addHandler(numberHandler)
        .addHandler(defaultHandler);

Optional<String> result = chain.process("12345");
```

## 📊 性能优化与最佳实践

### 观察者模式优化

```java
// 异步观察者模式
@Slf4j
public class AsyncObservable<T> {
    private final ExecutorService executorService;
    private final List<Consumer<T>> observers = new CopyOnWriteArrayList<>();
    
    public AsyncObservable(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }
    
    public void subscribe(Consumer<T> observer) {
        observers.add(observer);
    }
    
    public CompletableFuture<Void> publishAsync(T event) {
        List<CompletableFuture<Void>> futures = observers.stream()
                .map(observer -> CompletableFuture.runAsync(() -> {
                    try {
                        observer.accept(event);
                    } catch (Exception e) {
                        log.error("异步观察者处理事件失败", e);
                    }
                }, executorService))
                .collect(Collectors.toList());
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }
    
    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
    }
}
```

### 策略模式优化 - 策略缓存

```java
@Component
public class CachedStrategyFactory {
    private final Map<String, PaymentStrategy> strategyCache = new ConcurrentHashMap<>();
    private final ApplicationContext applicationContext;
    
    public CachedStrategyFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        initializeStrategies();
    }
    
    private void initializeStrategies() {
        Map<String, PaymentStrategy> strategies = 
            applicationContext.getBeansOfType(PaymentStrategy.class);
        
        strategies.values().forEach(strategy -> 
            strategyCache.put(strategy.getPaymentMethod(), strategy));
    }
    
    public PaymentStrategy getStrategy(String paymentMethod) {
        return strategyCache.get(paymentMethod);
    }
    
    public Set<String> getAvailableStrategies() {
        return strategyCache.values().stream()
                .filter(PaymentStrategy::isAvailable)
                .map(PaymentStrategy::getPaymentMethod)
                .collect(Collectors.toSet());
    }
}
```

## 🎯 模式选择指南

### 使用场景决策表

| 需求描述 | 推荐模式 | 关键特征 |
|---------|---------|---------|
| 需要通知多个对象状态变化 | 观察者模式 | 一对多依赖 |
| 算法需要在运行时切换 | 策略模式 | 算法族替换 |
| 需要支持撤销/重做操作 | 命令模式 | 操作封装 |
| 有固定的处理流程骨架 | 模板方法模式 | 算法骨架 |
| 请求需要多个对象处理 | 责任链模式 | 链式处理 |
| 对象行为随状态改变 | 状态模式 | 状态转换 |
| 需要在不修改类的前提下增加操作 | 访问者模式 | 操作外置 |

### 性能考虑

1. **观察者模式** - 考虑异步通知，避免阻塞
2. **策略模式** - 使用缓存避免重复创建策略对象
3. **命令模式** - 限制历史记录大小，避免内存泄漏
4. **责任链模式** - 优化链长度，避免过长的处理链

## 📈 总结

行为型模式为我们提供了强大的工具来管理对象间的交互：

1. **观察者模式** - 实现松耦合的事件通知
2. **策略模式** - 封装算法族，支持运行时切换
3. **命令模式** - 封装请求，支持撤销和队列操作
4. **模板方法模式** - 定义算法骨架，子类实现细节
5. **责任链模式** - 链式处理请求，避免发送者与接收者耦合

### 核心原则

- **开闭原则** - 对扩展开放，对修改关闭
- **单一职责** - 每个类只负责一个职责
- **依赖倒置** - 依赖抽象而不是具体实现
- **最少知识原则** - 减少对象间的直接依赖

通过合理应用行为型模式，我们能够构建出更加灵活、可维护和可扩展的Java应用程序。

## 🔗 相关资源

- [项目源代码](https://github.com/Rise1024/Java-Labs/tree/main/java-design-patterns#readme)
- [Java设计模式系列文章](../README.md)
- [创建型模式详解](./01-Java设计模式实战：创建型模式深度解析.md)
- [结构型模式详解](./02-Java设计模式实战：结构型模式深度解析.md)
- [牛逼的博客网站](https://dongsheng.online)
