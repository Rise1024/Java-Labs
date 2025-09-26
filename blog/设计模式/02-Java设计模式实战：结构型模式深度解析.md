---
title: Java设计模式实战：结构型模式深度解析
description: 深入解析Java结构型设计模式，包括适配器、装饰器、代理等模式的实现原理和最佳实践
tags: [Java, 设计模式, 结构型模式, 适配器模式, 装饰器模式, 代理模式]
category: 设计模式
date: 2025-09-26
---

# Java设计模式实战：结构型模式深度解析

## 🎯 引言

结构型模式关注类和对象的组合，描述如何将类或对象结合在一起形成更大的结构。这些模式通过合理的组合来实现更强大的功能，同时保持结构的灵活性和可扩展性。

本文将深入解析最重要的结构型设计模式，结合现代Java开发实践，提供可直接应用于生产环境的解决方案。

## 📚 结构型模式概述

结构型模式主要解决以下问题：
- **接口不兼容** - 通过适配器连接不兼容的接口
- **功能扩展** - 在不修改原类的情况下增加新功能
- **访问控制** - 控制对象的访问方式和权限
- **结构简化** - 为复杂的子系统提供简单的接口

### 结构型模式分类

| 模式名称 | 主要目的 | 适用场景 | 复杂度 |
|---------|---------|---------|-------|
| **适配器模式** | 接口转换 | 整合第三方库、遗留系统集成 | ⭐⭐⭐ |
| **装饰器模式** | 动态扩展功能 | 功能组合、中间件、AOP | ⭐⭐⭐⭐ |
| **代理模式** | 控制访问 | 缓存、权限控制、延迟加载 | ⭐⭐⭐⭐ |
| **外观模式** | 简化接口 | API网关、统一入口 | ⭐⭐ |
| **桥接模式** | 分离抽象和实现 | 跨平台开发、驱动程序 | ⭐⭐⭐⭐⭐ |
| **组合模式** | 树形结构 | 文件系统、组织架构 | ⭐⭐⭐⭐ |
| **享元模式** | 共享对象 | 缓存、对象池 | ⭐⭐⭐⭐⭐ |

## 🔌 适配器模式详解

### 模式定义

将一个类的接口转换成客户希望的另一个接口，使得原本由于接口不兼容而不能一起工作的类可以一起工作。

### 实现方式

#### 1. 对象适配器（推荐）

使用组合的方式，更灵活且符合"组合优于继承"的原则。

```java
// 目标接口 - 客户端期望的接口
public interface Target {
    String request(String data);
}

// 被适配者 - 需要适配的现有类
@Slf4j
public class Adaptee {
    public String specificRequest(String input) {
        String result = "Adaptee处理: " + input.toUpperCase();
        log.info("被适配者处理请求: {} -> {}", input, result);
        return result;
    }
}

// 对象适配器
@Slf4j
@AllArgsConstructor
public class ObjectAdapter implements Target {
    private final Adaptee adaptee;
    
    @Override
    public String request(String data) {
        log.info("对象适配器转换请求: {}", data);
        // 将Target接口的调用转换为Adaptee的调用
        return adaptee.specificRequest(data);
    }
}
```

#### 2. 类适配器

使用继承的方式，在Java中由于单继承限制，使用较少。

```java
@Slf4j
public class ClassAdapter extends Adaptee implements Target {
    @Override
    public String request(String data) {
        log.info("类适配器转换请求: {}", data);
        return specificRequest(data);
    }
}
```

### 实际应用场景

#### 1. 第三方库集成

```java
// 第三方支付接口
public interface ThirdPartyPayment {
    PaymentResult processPayment(String amount, String currency);
}

// 我们的统一支付接口
public interface PaymentService {
    boolean pay(double amount, String currency);
}

// 适配器实现
@Slf4j
@AllArgsConstructor
public class PaymentAdapter implements PaymentService {
    private final ThirdPartyPayment thirdPartyPayment;
    
    @Override
    public boolean pay(double amount, String currency) {
        log.info("适配器处理支付请求: {} {}", amount, currency);
        
        try {
            PaymentResult result = thirdPartyPayment.processPayment(
                String.valueOf(amount), currency);
            return result.isSuccess();
        } catch (Exception e) {
            log.error("支付处理失败", e);
            return false;
        }
    }
}
```

#### 2. 数据格式转换

```java
// 现有的JSON处理器
public class JsonProcessor {
    public String processJson(String json) {
        // JSON处理逻辑
        return json.toUpperCase();
    }
}

// 需要XML处理能力
public interface XmlProcessor {
    String processXml(String xml);
}

// XML到JSON适配器
@AllArgsConstructor
public class XmlToJsonAdapter implements XmlProcessor {
    private final JsonProcessor jsonProcessor;
    
    @Override
    public String processXml(String xml) {
        // 将XML转换为JSON
        String json = convertXmlToJson(xml);
        // 使用现有的JSON处理器
        String processedJson = jsonProcessor.processJson(json);
        // 转换回XML
        return convertJsonToXml(processedJson);
    }
    
    private String convertXmlToJson(String xml) {
        // XML到JSON的转换逻辑
        return "{}"; // 简化实现
    }
    
    private String convertJsonToXml(String json) {
        // JSON到XML的转换逻辑
        return "<root></root>"; // 简化实现
    }
}
```

## 🎨 装饰器模式详解

### 模式定义

动态地给对象添加一些额外的职责，就增加功能来说，装饰器模式比生成子类更为灵活。

### 核心实现

```java
// 组件接口
public interface Component {
    String operation();
    double getCost();
}

// 具体组件 - 基础咖啡
@Slf4j
public class ConcreteComponent implements Component {
    @Override
    public String operation() {
        String result = "基础咖啡";
        log.info("制作: {}", result);
        return result;
    }
    
    @Override
    public double getCost() {
        return 10.0;
    }
}

// 抽象装饰器
@AllArgsConstructor
public abstract class BaseDecorator implements Component {
    protected final Component component;
    
    @Override
    public String operation() {
        return component.operation();
    }
    
    @Override
    public double getCost() {
        return component.getCost();
    }
}

// 具体装饰器 - 牛奶装饰器
@Slf4j
public class MilkDecorator extends BaseDecorator {
    public MilkDecorator(Component component) {
        super(component);
    }
    
    @Override
    public String operation() {
        String result = super.operation() + " + 牛奶";
        log.info("添加牛奶装饰: {}", result);
        return result;
    }
    
    @Override
    public double getCost() {
        return super.getCost() + 2.0;
    }
}
```

### 企业级应用场景

#### 1. Web请求处理管道

```java
// HTTP请求处理接口
public interface RequestHandler {
    Response handle(Request request);
}

// 基础处理器
public class BaseRequestHandler implements RequestHandler {
    @Override
    public Response handle(Request request) {
        return new Response("基础处理完成");
    }
}

// 认证装饰器
@Slf4j
public class AuthenticationDecorator extends BaseDecorator {
    public AuthenticationDecorator(RequestHandler handler) {
        super(handler);
    }
    
    @Override
    public Response handle(Request request) {
        log.info("执行身份认证");
        
        if (!isAuthenticated(request)) {
            return new Response("认证失败", 401);
        }
        
        return super.handle(request);
    }
    
    private boolean isAuthenticated(Request request) {
        // 认证逻辑
        return request.getHeader("Authorization") != null;
    }
}

// 日志装饰器
@Slf4j
public class LoggingDecorator extends BaseDecorator {
    public LoggingDecorator(RequestHandler handler) {
        super(handler);
    }
    
    @Override
    public Response handle(Request request) {
        long startTime = System.currentTimeMillis();
        log.info("开始处理请求: {}", request.getPath());
        
        try {
            Response response = super.handle(request);
            long duration = System.currentTimeMillis() - startTime;
            log.info("请求处理完成: {} - 耗时: {}ms", 
                    request.getPath(), duration);
            return response;
        } catch (Exception e) {
            log.error("请求处理失败: {}", request.getPath(), e);
            throw e;
        }
    }
}

// 使用示例
public class RequestProcessingDemo {
    public static void main(String[] args) {
        RequestHandler handler = new LoggingDecorator(
            new AuthenticationDecorator(
                new BaseRequestHandler()
            )
        );
        
        Request request = new Request("/api/users");
        Response response = handler.handle(request);
    }
}
```

#### 2. 数据缓存装饰器

```java
// 数据服务接口
public interface DataService {
    String getData(String key);
    void saveData(String key, String data);
}

// 基础数据服务
@Slf4j
public class DatabaseDataService implements DataService {
    @Override
    public String getData(String key) {
        log.info("从数据库获取数据: {}", key);
        // 模拟数据库查询
        return "数据库数据: " + key;
    }
    
    @Override
    public void saveData(String key, String data) {
        log.info("保存数据到数据库: {} = {}", key, data);
    }
}

// 缓存装饰器
@Slf4j
public class CacheDecorator implements DataService {
    private final DataService dataService;
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    public CacheDecorator(DataService dataService) {
        this.dataService = dataService;
    }
    
    @Override
    public String getData(String key) {
        // 先查缓存
        String cachedData = cache.get(key);
        if (cachedData != null) {
            log.info("从缓存获取数据: {}", key);
            return cachedData;
        }
        
        // 缓存未命中，查询数据库
        String data = dataService.getData(key);
        cache.put(key, data);
        log.info("数据已缓存: {}", key);
        
        return data;
    }
    
    @Override
    public void saveData(String key, String data) {
        dataService.saveData(key, data);
        cache.put(key, data); // 更新缓存
    }
}
```

## 🛡️ 代理模式详解

### 模式定义

为其他对象提供一种代理以控制对这个对象的访问。

### 代理类型

1. **虚拟代理** - 延迟创建开销大的对象
2. **保护代理** - 控制对原对象的访问权限
3. **智能代理** - 在访问对象时执行一些附加操作

### 核心实现

```java
// 主题接口
public interface Subject {
    String request(String request);
}

// 真实主题
@Slf4j
public class RealSubject implements Subject {
    @Override
    public String request(String request) {
        log.info("真实主题处理请求: {}", request);
        
        // 模拟耗时操作
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "真实处理结果: " + request.toUpperCase();
    }
}

// 代理类
@Slf4j
public class Proxy implements Subject {
    private RealSubject realSubject;
    private final Map<String, String> cache = new HashMap<>();
    
    @Override
    public String request(String request) {
        log.info("代理接收请求: {}", request);
        
        // 1. 预处理 - 权限检查
        if (!checkAccess(request)) {
            return "访问被拒绝: " + request;
        }
        
        // 2. 缓存检查
        if (cache.containsKey(request)) {
            String cachedResult = cache.get(request);
            log.info("从缓存返回结果: {}", cachedResult);
            return cachedResult;
        }
        
        // 3. 延迟初始化真实对象
        if (realSubject == null) {
            log.info("延迟创建真实主题对象");
            realSubject = new RealSubject();
        }
        
        // 4. 调用真实对象
        String result = realSubject.request(request);
        
        // 5. 后处理 - 缓存结果
        cache.put(request, result);
        log.info("缓存处理结果: {}", request);
        
        return result;
    }
    
    private boolean checkAccess(String request) {
        boolean allowed = !request.contains("forbidden");
        log.info("权限检查 [{}]: {}", request, allowed ? "允许" : "拒绝");
        return allowed;
    }
}
```

### 动态代理应用

Java提供了动态代理机制，可以在运行时创建代理对象。

#### 1. JDK动态代理

```java
// 性能监控代理
@Slf4j
public class PerformanceInvocationHandler implements InvocationHandler {
    private final Object target;
    
    public PerformanceInvocationHandler(Object target) {
        this.target = target;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) 
            throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = method.invoke(target, args);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("方法 {} 执行耗时: {}ms", method.getName(), duration);
            
            return result;
        } catch (Exception e) {
            log.error("方法 {} 执行失败", method.getName(), e);
            throw e;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(T target, Class<T> interfaceType) {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class[]{interfaceType},
            new PerformanceInvocationHandler(target)
        );
    }
}

// 使用示例
public class DynamicProxyDemo {
    public static void main(String[] args) {
        // 创建真实对象
        DataService realService = new DatabaseDataService();
        
        // 创建代理对象
        DataService proxyService = PerformanceInvocationHandler
            .createProxy(realService, DataService.class);
        
        // 通过代理调用方法
        String data = proxyService.getData("test");
        log.info("获取到数据: {}", data);
    }
}
```

#### 2. CGLIB动态代理

适用于没有接口的类，通过字节码生成技术创建子类代理。

```java
@Slf4j
public class CglibMethodInterceptor implements MethodInterceptor {
    
    @Override
    public Object intercept(Object obj, Method method, Object[] args, 
                          MethodProxy proxy) throws Throwable {
        log.info("CGLIB代理 - 方法调用前: {}", method.getName());
        
        Object result = proxy.invokeSuper(obj, args);
        
        log.info("CGLIB代理 - 方法调用后: {}", method.getName());
        
        return result;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibMethodInterceptor());
        
        return (T) enhancer.create();
    }
}
```

### 企业级代理应用

#### 1. 分布式服务代理

```java
// 远程服务接口
public interface RemoteService {
    String callRemoteApi(String request);
}

// 远程服务代理
@Slf4j
public class RemoteServiceProxy implements RemoteService {
    private final String serviceUrl;
    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    
    public RemoteServiceProxy(String serviceUrl) {
        this.serviceUrl = serviceUrl;
        this.restTemplate = new RestTemplate();
        this.circuitBreaker = CircuitBreaker.ofDefaults("remoteService");
    }
    
    @Override
    public String callRemoteApi(String request) {
        return circuitBreaker.executeSupplier(() -> {
            try {
                log.info("调用远程服务: {}", serviceUrl);
                
                // 服务发现
                String actualUrl = discoverService(serviceUrl);
                
                // 负载均衡
                String selectedUrl = loadBalance(actualUrl);
                
                // 发起HTTP请求
                return restTemplate.postForObject(
                    selectedUrl, request, String.class);
                    
            } catch (Exception e) {
                log.error("远程服务调用失败", e);
                throw new RuntimeException("服务不可用", e);
            }
        });
    }
    
    private String discoverService(String serviceName) {
        // 服务发现逻辑
        return "http://actual-service:8080/api";
    }
    
    private String loadBalance(String baseUrl) {
        // 负载均衡逻辑
        return baseUrl;
    }
}
```

#### 2. 数据库连接代理

```java
// 数据库连接代理 - 实现连接池和事务管理
@Slf4j
public class ConnectionProxy implements Connection {
    private final Connection realConnection;
    private final ConnectionPool connectionPool;
    private boolean closed = false;
    
    public ConnectionProxy(Connection realConnection, 
                          ConnectionPool connectionPool) {
        this.realConnection = realConnection;
        this.connectionPool = connectionPool;
    }
    
    @Override
    public void close() throws SQLException {
        if (!closed) {
            log.info("归还连接到连接池");
            connectionPool.returnConnection(this);
            closed = true;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        log.info("预编译SQL: {}", sql);
        return realConnection.prepareStatement(sql);
    }
    
    @Override
    public void commit() throws SQLException {
        log.info("提交事务");
        realConnection.commit();
    }
    
    @Override
    public void rollback() throws SQLException {
        log.info("回滚事务");
        realConnection.rollback();
    }
    
    // 其他Connection方法的委托实现...
}
```

## 🏗️ 其他重要结构型模式

### 外观模式（Facade Pattern）

提供一个统一的接口来访问子系统中的一群接口。

```java
// 复杂子系统
@Slf4j
public class OrderSubsystem {
    public void createOrder(String orderId) {
        log.info("创建订单: {}", orderId);
    }
}

@Slf4j
public class PaymentSubsystem {
    public boolean processPayment(String orderId, double amount) {
        log.info("处理支付: {} - {}", orderId, amount);
        return true;
    }
}

@Slf4j
public class InventorySubsystem {
    public boolean reserveItems(String orderId, List<String> items) {
        log.info("预留库存: {} - {}", orderId, items);
        return true;
    }
}

// 外观类 - 简化复杂的子系统交互
@Slf4j
@AllArgsConstructor
public class OrderFacade {
    private final OrderSubsystem orderSubsystem;
    private final PaymentSubsystem paymentSubsystem;
    private final InventorySubsystem inventorySubsystem;
    
    public boolean placeOrder(String orderId, List<String> items, double amount) {
        log.info("开始处理订单: {}", orderId);
        
        try {
            // 1. 创建订单
            orderSubsystem.createOrder(orderId);
            
            // 2. 预留库存
            if (!inventorySubsystem.reserveItems(orderId, items)) {
                throw new RuntimeException("库存不足");
            }
            
            // 3. 处理支付
            if (!paymentSubsystem.processPayment(orderId, amount)) {
                throw new RuntimeException("支付失败");
            }
            
            log.info("订单处理成功: {}", orderId);
            return true;
            
        } catch (Exception e) {
            log.error("订单处理失败: {}", orderId, e);
            return false;
        }
    }
}
```

### 组合模式（Composite Pattern）

将对象组合成树形结构以表示"部分-整体"的层次结构。

```java
// 组件接口
public interface FileSystemComponent {
    void display(int depth);
    long getSize();
    String getName();
}

// 文件（叶子节点）
@AllArgsConstructor
@Getter
public class File implements FileSystemComponent {
    private final String name;
    private final long size;
    
    @Override
    public void display(int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "📄 " + name + " (" + size + " bytes)");
    }
    
    @Override
    public long getSize() {
        return size;
    }
}

// 目录（组合节点）
@Getter
public class Directory implements FileSystemComponent {
    private final String name;
    private final List<FileSystemComponent> children = new ArrayList<>();
    
    public Directory(String name) {
        this.name = name;
    }
    
    public void add(FileSystemComponent component) {
        children.add(component);
    }
    
    public void remove(FileSystemComponent component) {
        children.remove(component);
    }
    
    @Override
    public void display(int depth) {
        String indent = "  ".repeat(depth);
        System.out.println(indent + "📁 " + name + "/");
        
        for (FileSystemComponent child : children) {
            child.display(depth + 1);
        }
    }
    
    @Override
    public long getSize() {
        return children.stream()
                .mapToLong(FileSystemComponent::getSize)
                .sum();
    }
}
```

## 🚀 现代Java特性的应用

### 使用接口的默认方法简化装饰器

```java
public interface EnhancedComponent {
    String process(String input);
    
    // 默认的日志装饰
    default EnhancedComponent withLogging() {
        return input -> {
            System.out.println("处理输入: " + input);
            String result = this.process(input);
            System.out.println("处理结果: " + result);
            return result;
        };
    }
    
    // 默认的缓存装饰
    default EnhancedComponent withCaching() {
        Map<String, String> cache = new ConcurrentHashMap<>();
        return input -> cache.computeIfAbsent(input, this::process);
    }
    
    // 默认的性能监控装饰
    default EnhancedComponent withPerformanceMonitoring() {
        return input -> {
            long start = System.currentTimeMillis();
            String result = this.process(input);
            long duration = System.currentTimeMillis() - start;
            System.out.println("处理耗时: " + duration + "ms");
            return result;
        };
    }
}

// 使用示例
EnhancedComponent component = input -> "处理: " + input.toUpperCase();

EnhancedComponent enhancedComponent = component
    .withLogging()
    .withCaching()
    .withPerformanceMonitoring();
```

### 使用函数式接口实现轻量级代理

```java
@FunctionalInterface
public interface ServiceFunction<T, R> {
    R apply(T input) throws Exception;
}

public class FunctionalProxy {
    
    public static <T, R> ServiceFunction<T, R> withRetry(
            ServiceFunction<T, R> function, int maxRetries) {
        return input -> {
            Exception lastException = null;
            
            for (int i = 0; i <= maxRetries; i++) {
                try {
                    return function.apply(input);
                } catch (Exception e) {
                    lastException = e;
                    if (i < maxRetries) {
                        Thread.sleep(1000 * (i + 1)); // 指数退避
                    }
                }
            }
            
            throw lastException;
        };
    }
    
    public static <T, R> ServiceFunction<T, R> withCircuitBreaker(
            ServiceFunction<T, R> function, int failureThreshold) {
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicLong lastFailureTime = new AtomicLong(0);
        
        return input -> {
            long currentTime = System.currentTimeMillis();
            
            // 检查熔断器状态
            if (failureCount.get() >= failureThreshold) {
                if (currentTime - lastFailureTime.get() < 60000) { // 1分钟熔断
                    throw new RuntimeException("服务熔断中");
                } else {
                    failureCount.set(0); // 重置计数器
                }
            }
            
            try {
                R result = function.apply(input);
                failureCount.set(0); // 成功时重置计数器
                return result;
            } catch (Exception e) {
                failureCount.incrementAndGet();
                lastFailureTime.set(currentTime);
                throw e;
            }
        };
    }
}

// 使用示例
ServiceFunction<String, String> service = input -> {
    // 可能失败的服务调用
    if (Math.random() < 0.3) {
        throw new RuntimeException("服务调用失败");
    }
    return "处理结果: " + input;
};

ServiceFunction<String, String> resilientService = 
    FunctionalProxy.withCircuitBreaker(
        FunctionalProxy.withRetry(service, 3), 
        5
    );
```

## 📊 性能优化与最佳实践

### 装饰器模式优化

```java
// 使用建造者模式构建装饰器链
public class DecoratorChainBuilder {
    private Component component;
    
    public DecoratorChainBuilder(Component component) {
        this.component = component;
    }
    
    public DecoratorChainBuilder addLogging() {
        this.component = new LoggingDecorator(component);
        return this;
    }
    
    public DecoratorChainBuilder addCaching() {
        this.component = new CachingDecorator(component);
        return this;
    }
    
    public DecoratorChainBuilder addAuthentication() {
        this.component = new AuthenticationDecorator(component);
        return this;
    }
    
    public Component build() {
        return component;
    }
}

// 使用示例
Component decoratedComponent = new DecoratorChainBuilder(baseComponent)
    .addAuthentication()
    .addLogging()
    .addCaching()
    .build();
```

### 代理模式性能优化

```java
// 使用ThreadLocal优化代理性能
public class OptimizedProxy implements Subject {
    private final ThreadLocal<Subject> realSubjectHolder = new ThreadLocal<>();
    private final Subject sharedRealSubject;
    
    public OptimizedProxy() {
        this.sharedRealSubject = new RealSubject();
    }
    
    @Override
    public String request(String request) {
        // 对于线程安全的操作，使用共享实例
        if (isThreadSafeOperation(request)) {
            return sharedRealSubject.request(request);
        }
        
        // 对于非线程安全的操作，使用ThreadLocal实例
        Subject realSubject = realSubjectHolder.get();
        if (realSubject == null) {
            realSubject = new RealSubject();
            realSubjectHolder.set(realSubject);
        }
        
        return realSubject.request(request);
    }
    
    private boolean isThreadSafeOperation(String request) {
        // 判断操作是否线程安全
        return request.startsWith("READ_");
    }
}
```

## 🎯 模式选择指南

### 决策矩阵

| 需求场景 | 推荐模式 | 关键考虑因素 |
|---------|---------|-------------|
| 整合第三方库 | 适配器模式 | 接口兼容性 |
| 动态添加功能 | 装饰器模式 | 功能组合灵活性 |
| 控制对象访问 | 代理模式 | 访问控制需求 |
| 简化复杂接口 | 外观模式 | 接口复杂度 |
| 树形数据结构 | 组合模式 | 层次结构需求 |
| 大量相似对象 | 享元模式 | 内存使用效率 |
| 跨平台抽象 | 桥接模式 | 平台独立性 |

### 反模式警告

1. **过度装饰** - 装饰器层次过深影响性能
2. **代理滥用** - 不必要的代理增加复杂性
3. **适配器链** - 多重适配器转换降低效率

## 📈 总结

结构型模式是构建灵活、可扩展系统的重要工具：

1. **适配器模式** - 解决接口不兼容问题
2. **装饰器模式** - 实现功能的动态组合
3. **代理模式** - 提供访问控制和增强功能
4. **外观模式** - 简化复杂子系统的接口
5. **组合模式** - 处理树形结构数据
6. **享元模式** - 优化内存使用
7. **桥接模式** - 分离抽象和实现

### 关键原则

- **组合优于继承** - 使用组合实现功能扩展
- **开闭原则** - 对扩展开放，对修改关闭
- **单一职责** - 每个装饰器只负责一个功能
- **性能考虑** - 避免过度包装影响性能

通过合理应用结构型模式，我们能够构建出更加灵活、可维护和高性能的Java应用程序。

## 🔗 相关资源

- [项目源代码](https://github.com/Rise1024/Java-Labs/tree/main/java-design-patterns#readme)
- [Java设计模式系列文章](../README.md)
- [创建型模式详解](./01-Java设计模式实战：创建型模式深度解析.md)
- [牛逼的博客网站](https://dongsheng.online)
