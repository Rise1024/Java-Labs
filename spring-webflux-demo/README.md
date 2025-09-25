# Spring WebFlux 响应式编程学习项目

基于 Spring WebFlux 的响应式编程实战项目，对应博文《Spring WebFlux 响应式编程详解》。

## 🎯 项目简介

本项目演示了 Spring WebFlux 的核心功能和最佳实践：

- **响应式编程模型** - 基于 Reactor 的 Mono/Flux 流式处理
- **注解式控制器** - 传统的响应式 REST API 开发
- **函数式端点** - 函数式编程风格的路由处理
- **WebClient 客户端** - 响应式 HTTP 客户端调用
- **异常处理** - 全局响应式异常处理机制
- **性能监控** - 完整的监控和健康检查

## 🛠️ 技术栈

- **Spring Boot** 3.5.6
- **Spring Framework** 6.2.11
- **Spring WebFlux** 响应式Web框架
- **Spring Data R2DBC** 响应式数据库访问
- **Project Reactor** 响应式编程库
- **MySQL** 关系型数据库（统一使用）
- **Redis** 缓存支持
- **Micrometer** 监控指标

## 📁 项目结构

```
src/main/java/com/javalaabs/webflux/
├── WebFluxDemoApplication.java           # 应用启动类
├── config/                               # 配置类
│   ├── WebFluxConfig.java               # WebFlux配置
│   ├── R2dbcConfig.java                 # R2DBC数据库配置
│   ├── RedisConfig.java                 # Redis缓存配置
│   └── RouterConfiguration.java         # 函数式路由配置
├── controller/                           # 注解式控制器
│   └── ReactiveUserController.java      # 用户REST控制器
├── handler/                              # 函数式处理器
│   ├── UserHandler.java                 # 用户业务处理器
│   └── HealthHandler.java               # 健康检查处理器
├── service/                              # 业务服务层
│   ├── ReactiveUserService.java         # 用户业务服务
│   └── ExternalApiService.java          # 外部API调用服务
├── repository/                           # 数据访问层
│   └── ReactiveUserRepository.java      # 用户响应式Repository
├── domain/                               # 领域对象
│   ├── entity/                          # 实体类
│   │   ├── User.java                    # 用户实体
│   │   └── UserActivity.java           # 用户活动实体
│   ├── dto/                             # 数据传输对象
│   │   ├── UserDTO.java                 # 用户DTO
│   │   ├── CreateUserRequest.java       # 创建用户请求
│   │   └── UpdateUserRequest.java       # 更新用户请求
│   └── event/                           # 事件对象
│       └── UserCreatedEvent.java        # 用户创建事件
├── exception/                            # 异常处理
│   ├── GlobalWebExceptionHandler.java   # 全局异常处理器
│   ├── BusinessException.java           # 业务异常基类
│   └── UserNotFoundException.java       # 用户不存在异常
└── monitoring/                           # 监控相关
    ├── PerformanceMonitor.java          # 性能监控
    └── RequestMonitoringFilter.java     # 请求监控过滤器
```

## 🚀 快速开始

### 1. 环境要求

- **JDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+** (所有环境统一使用)
- **Redis** (可选，用于缓存)

### 2. 数据库准备

#### MySQL 安装和配置

```bash
# 使用 Docker 启动 MySQL
docker run -d \
  --name mysql-webflux \
  -e MYSQL_ROOT_PASSWORD=root123 \
  -e MYSQL_DATABASE=webflux_demo \
  -e MYSQL_USER=webflux_user \
  -e MYSQL_PASSWORD=webflux_password \
  -p 3306:3306 \
  mysql:8.0

# 手动执行初始化脚本（推荐）
mysql -u root -p < mysql-setup.sql

# 这将创建以下数据库：
# - webflux_demo (生产环境)
# - webflux_demo_dev (开发环境)  
# - webflux_demo_test (测试环境)
```

#### Redis 启动 (可选)

```bash
# 启动Redis用于缓存
docker run -d -p 6379:6379 redis:latest
```

### 3. 启动应用

```bash
# 克隆项目
cd spring-webflux-demo

# 开发环境启动
mvn clean spring-boot:run -Dspring-boot.run.profiles=dev

# 或者测试环境启动（使用MySQL测试数据库）
mvn clean spring-boot:run -Dspring-boot.run.profiles=test
```

### 3. 访问应用

- **应用地址**: http://localhost:8080
- **健康检查**: http://localhost:8080/actuator/health
- **API文档**: http://localhost:8080/swagger-ui.html

## 📚 核心功能演示

### 1. 注解式控制器 API

```bash
# 获取用户列表
curl "http://localhost:8080/api/reactive/users?page=0&size=10"

# 获取单个用户
curl "http://localhost:8080/api/reactive/users/{id}"

# 创建用户
curl -X POST "http://localhost:8080/api/reactive/users" \
  -H "Content-Type: application/json" \
  -d '{"name":"张三","email":"zhangsan@example.com"}'

# 服务器推送事件 (SSE)
curl "http://localhost:8080/api/reactive/users/stream"

# 流式JSON响应
curl -H "Accept: application/x-ndjson" \
  "http://localhost:8080/api/reactive/users/live-updates"
```

### 2. 函数式端点 API

```bash
# 函数式路由访问
curl "http://localhost:8080/api/users"
curl "http://localhost:8080/api/users/{id}"

# SSE 事件流
curl "http://localhost:8080/api/users/sse?userId=user123"

# 批量操作
curl -X POST "http://localhost:8080/api/users/batch" \
  -H "Content-Type: application/json" \
  -d '[{"operation":"CREATE","createRequest":{"name":"李四","email":"lisi@example.com"}}]'
```

### 3. WebClient 客户端示例

项目中集成了 WebClient 的各种使用场景：

- **基础 HTTP 调用**
- **并行请求聚合**
- **链式请求处理**
- **流式数据处理**
- **断路器模式**
- **请求缓存**

## 🔧 核心特性说明

### 1. 响应式编程模式

```java
// Mono - 0或1个元素
public Mono<UserDTO> getUser(String id) {
    return userService.findById(id)
                     .map(this::convertToDTO)
                     .doOnNext(user -> log.info("获取用户: {}", user.getName()));
}

// Flux - 0到N个元素
public Flux<UserDTO> getAllUsers() {
    return userService.findAll()
                     .map(this::convertToDTO)
                     .take(100);
}
```

### 2. 背压处理

```java
// 限制速率和缓冲
public Flux<String> handleBackpressure() {
    return Flux.range(1, 1000000)
              .map(i -> "数据-" + i)
              .onBackpressureBuffer(1000)  // 缓冲1000个元素
              .limitRate(100);              // 限制请求速率
}
```

### 3. 错误处理

```java
// 多种错误处理策略
public Mono<String> errorHandlingExample() {
    return Mono.fromCallable(() -> riskyOperation())
              .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
              .timeout(Duration.ofSeconds(5))
              .onErrorResume(TimeoutException.class, 
                  error -> Mono.just("操作超时，返回默认值"));
}
```

## 📊 监控和指标

### 1. 健康检查

```bash
# 基础健康检查
curl http://localhost:8080/actuator/health

# 详细健康信息
curl http://localhost:8080/actuator/health/detailed
```

### 2. Micrometer 指标

- `webflux.request.duration` - 请求处理时间
- `webflux.request.count` - 请求计数
- `webflux.connections.active` - 活跃连接数

### 3. 自定义监控

项目实现了完整的请求监控和性能指标收集：

- **请求追踪** - 唯一请求ID和执行时间
- **异常监控** - 详细的异常信息记录
- **性能指标** - 吞吐量和延迟监控

## 🎯 学习要点

1. **响应式编程思维** - 从阻塞式转向事件驱动
2. **Reactor 操作符** - 掌握 Mono/Flux 的各种操作符
3. **WebFlux vs MVC** - 理解两种编程模型的差异
4. **错误处理策略** - 响应式环境下的异常处理
5. **性能调优** - 调度器、缓冲区等配置优化

## 📖 相关博文

本项目对应博文：[Spring WebFlux 响应式编程详解](../blog/Spring/03-Web开发技术/Spring%20WebFlux%20响应式编程详解.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来完善这个学习项目！

## 📄 许可证

MIT License
