---
title: Spring WebFlux 响应式编程详解
description: Spring WebFlux 响应式编程详解
tags: [Spring WebFlux, Reactive Programming]
category: Spring
date: 2025-09-25
---

# Spring WebFlux 响应式编程详解

## 🎯 概述

Spring WebFlux 是 Spring 5.0 引入的响应式 Web 框架，它基于 Reactor 库构建，为构建非阻塞、异步的 Web 应用程序提供了完整的解决方案。根据 [Spring Framework 官方文档](https://docs.spring.io/spring-framework/reference/web.html)，WebFlux 支持函数式编程模式和注解式编程模式，能够有效处理高并发场景下的 I/O 密集型应用。

## 📚 响应式编程基础

### 1. 响应式编程核心概念

```
传统同步模型 vs 响应式异步模型

同步阻塞模型:
Request -> Thread -> Database -> Thread -> Response
(一个请求占用一个线程直到完成)

响应式非阻塞模型:
Request -> EventLoop -> Database(Async) -> Callback -> Response
(事件循环处理多个请求，非阻塞I/O)
```

### 2. Reactor 核心类型

```java
// Mono - 0或1个元素的发布者
public class ReactorBasics {

    // Mono 示例 - 单个值
    public Mono<String> getUser(Long id) {
        return Mono.fromCallable(() -> userService.findById(id))
                   .map(User::getName)
                   .doOnNext(name -> System.out.println("获取用户: " + name))
                   .doOnError(error -> System.err.println("获取用户失败: " + error.getMessage()))
                   .onErrorReturn("未知用户");
    }

    // Flux 示例 - 多个值的流
    public Flux<String> getAllUserNames() {
        return Flux.fromIterable(userService.findAll())
                   .map(User::getName)
                   .filter(name -> name.length() > 2)
                   .take(10)
                   .doOnNext(name -> System.out.println("处理用户: " + name))
                   .onErrorContinue((error, item) -> 
                       System.err.println("处理 " + item + " 时出错: " + error.getMessage()));
    }

    // 背压处理
    public Flux<String> handleBackpressure() {
        return Flux.range(1, 1000000)
                   .map(i -> "数据-" + i)
                   .onBackpressureBuffer(1000)  // 缓冲1000个元素
                   .limitRate(100);              // 限制请求速率
    }

    // 错误处理策略
    public Mono<String> errorHandlingExample() {
        return Mono.fromCallable(() -> riskyOperation())
                   .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))) // 重试策略
                   .timeout(Duration.ofSeconds(5))                      // 超时设置
                   .onErrorResume(error -> {                           // 错误恢复
                       if (error instanceof TimeoutException) {
                           return Mono.just("操作超时，返回默认值");
                       }
                       return Mono.error(new RuntimeException("操作失败", error));
                   });
    }

    // 操作符组合
    public Flux<UserOrder> combineStreams() {
        Flux<User> users = userService.getAllUsers();
        Flux<Order> orders = orderService.getAllOrders();

        return users
            .flatMap(user -> 
                orders.filter(order -> order.getUserId().equals(user.getId()))
                      .map(order -> new UserOrder(user, order))
            )
            .collectList()
            .flatMapMany(Flux::fromIterable)
            .sort((uo1, uo2) -> uo1.getOrder().getCreateTime().compareTo(uo2.getOrder().getCreateTime()));
    }

    private String riskyOperation() {
        if (Math.random() > 0.7) {
            throw new RuntimeException("随机失败");
        }
        return "操作成功";
    }
}
```

## 🚀 WebFlux 核心架构

### 1. WebFlux vs Spring MVC 架构对比

```
Spring MVC (Servlet Stack):
┌─────────────────────────────────────────┐
│          Servlet Container              │
│  ┌─────────────────────────────────────┐│
│  │        DispatcherServlet           ││
│  │  ┌─────────────┐ ┌─────────────┐   ││
│  │  │   Handler   │ │    View     │   ││
│  │  │   Mapping   │ │  Resolver   │   ││
│  │  └─────────────┘ └─────────────┘   ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
        │ (阻塞式，每请求一线程)
        ▼
┌─────────────────────────────────────────┐
│              Controller                 │
└─────────────────────────────────────────┘

Spring WebFlux (Reactive Stack):
┌─────────────────────────────────────────┐
│       Netty / Undertow / Jetty          │
│  ┌─────────────────────────────────────┐│
│  │      DispatcherHandler             ││
│  │  ┌─────────────┐ ┌─────────────┐   ││
│  │  │   Handler   │ │   Result    │   ││
│  │  │   Mapping   │ │   Handler   │   ││
│  │  └─────────────┘ └─────────────┘   ││
│  └─────────────────────────────────────┘│
└─────────────────────────────────────────┘
        │ (非阻塞式，事件循环)
        ▼
┌─────────────────────────────────────────┐
│    Controller / Functional Handler      │
└─────────────────────────────────────────┘
```

### 2. WebFlux 配置

```java
@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    // 路由器函数配置
    @Bean
    public RouterFunction<ServerResponse> routerFunction(UserHandler userHandler) {
        return RouterFunctions
            .route(GET("/api/users"), userHandler::getAllUsers)
            .andRoute(GET("/api/users/{id}"), userHandler::getUser)
            .andRoute(POST("/api/users"), userHandler::createUser)
            .andRoute(PUT("/api/users/{id}"), userHandler::updateUser)
            .andRoute(DELETE("/api/users/{id}"), userHandler::deleteUser)
            .andRoute(GET("/api/users/stream"), userHandler::streamUsers);
    }

    // CORS 配置
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // 资源处理配置
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS));
    }

    // HTTP 消息编解码器配置
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
        configurer.defaultCodecs().enableLoggingRequestDetails(true);
    }

    // 异常处理
    @Bean
    public WebExceptionHandler globalExceptionHandler() {
        return new GlobalWebExceptionHandler();
    }

    // 视图解析器
    @Bean
    public ReactiveViewResolver thymeleafViewResolver() {
        return new ThymeleafReactiveViewResolver(templateEngine());
    }

    @Bean
    public ISpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        return templateEngine;
    }

    private ITemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCacheable(true);
        return resolver;
    }
}
```

## 🎯 注解式控制器

### 1. 响应式控制器基础

```java
@RestController
@RequestMapping("/api/reactive/users")
public class ReactiveUserController {

    private final ReactiveUserService userService;
    private final ReactiveNotificationService notificationService;

    public ReactiveUserController(ReactiveUserService userService,
                                ReactiveNotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> getUser(@PathVariable String id) {
        return userService.findById(id)
                         .map(user -> ResponseEntity.ok(user))
                         .defaultIfEmpty(ResponseEntity.notFound().build())
                         .doOnNext(response -> System.out.println("返回用户: " + id))
                         .doOnError(error -> System.err.println("获取用户失败: " + error.getMessage()));
    }

    // 获取用户列表（支持分页）
    @GetMapping
    public Flux<UserDTO> getUsers(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String search) {
        
        return userService.findUsers(page, size, search)
                         .take(size)
                         .doOnNext(user -> System.out.println("返回用户: " + user.getName()))
                         .doOnComplete(() -> System.out.println("用户列表返回完成"));
    }

    // 创建用户
    @PostMapping
    public Mono<ResponseEntity<UserDTO>> createUser(@Valid @RequestBody Mono<CreateUserRequest> userRequest) {
        return userRequest
            .flatMap(request -> userService.createUser(request))
            .flatMap(savedUser -> 
                notificationService.sendWelcomeEmail(savedUser)
                                  .thenReturn(savedUser)
            )
            .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
            .onErrorResume(error -> {
                if (error instanceof ValidationException) {
                    return Mono.just(ResponseEntity.badRequest().build());
                }
                return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
            });
    }

    // 批量创建用户
    @PostMapping("/batch")
    public Flux<UserDTO> createUsers(@RequestBody Flux<CreateUserRequest> userRequests) {
        return userRequests
            .buffer(10) // 每10个一批处理
            .flatMap(batch -> 
                userService.createUsers(batch)
                          .onErrorContinue((error, user) -> 
                              System.err.println("创建用户失败: " + error.getMessage()))
            )
            .doOnNext(user -> System.out.println("批量创建用户: " + user.getName()));
    }

    // 更新用户
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody Mono<UpdateUserRequest> updateRequest) {
        return Mono.zip(userService.findById(id), updateRequest)
                  .flatMap(tuple -> {
                      UserDTO existingUser = tuple.getT1();
                      UpdateUserRequest request = tuple.getT2();
                      return userService.updateUser(existingUser.getId(), request);
                  })
                  .map(updatedUser -> ResponseEntity.ok(updatedUser))
                  .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userService.deleteById(id)
                         .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                         .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // 服务器推送事件 (SSE)
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UserActivityDTO>> streamUserActivities() {
        return userService.getUserActivityStream()
                         .map(activity -> ServerSentEvent.builder(activity)
                                                        .id(String.valueOf(activity.getId()))
                                                        .event("user-activity")
                                                        .build())
                         .doOnNext(event -> System.out.println("推送用户活动: " + event.data()))
                         .doOnCancel(() -> System.out.println("客户端取消了用户活动流"));
    }

    // WebSocket 样式的流式响应
    @GetMapping(value = "/live-updates", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<UserUpdateEvent> liveUserUpdates(@RequestParam(required = false) String userId) {
        Flux<UserUpdateEvent> stream = userService.getUserUpdateStream();
        
        if (userId != null) {
            stream = stream.filter(event -> userId.equals(event.getUserId()));
        }
        
        return stream
            .delayElements(Duration.ofMillis(100)) // 限制推送速率
            .doOnSubscribe(subscription -> System.out.println("客户端订阅用户更新流"))
            .doOnCancel(() -> System.out.println("客户端取消用户更新流"));
    }

    // 文件上传
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<String>> uploadAvatar(@PathVariable String id,
                                                    @RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono
            .flatMap(filePart -> userService.uploadAvatar(id, filePart))
            .map(avatarUrl -> ResponseEntity.ok(avatarUrl))
            .onErrorResume(error -> 
                Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                       .body("上传失败: " + error.getMessage())));
    }

    // 健康检查
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return userService.getHealthStatus()
                         .map(status -> ResponseEntity.ok(Map.of(
                             "status", status,
                             "timestamp", Instant.now(),
                             "service", "reactive-user-service"
                         )));
    }
}
```

### 2. 响应式数据处理

```java
@Service
public class ReactiveUserService {

    private final R2dbcEntityTemplate entityTemplate;
    private final ReactiveUserRepository userRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public ReactiveUserService(R2dbcEntityTemplate entityTemplate,
                             ReactiveUserRepository userRepository,
                             ReactiveRedisTemplate<String, Object> redisTemplate,
                             ApplicationEventPublisher eventPublisher) {
        this.entityTemplate = entityTemplate;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
    }

    // 缓存查询
    public Mono<UserDTO> findById(String id) {
        String cacheKey = "user:" + id;
        
        return redisTemplate.opsForValue().get(cacheKey)
                           .cast(UserDTO.class)
                           .switchIfEmpty(
                               userRepository.findById(id)
                                           .map(this::convertToDTO)
                                           .flatMap(userDTO -> 
                                               redisTemplate.opsForValue()
                                                          .set(cacheKey, userDTO, Duration.ofMinutes(30))
                                                          .thenReturn(userDTO)
                                           )
                           )
                           .doOnNext(user -> System.out.println("获取用户: " + user.getName()));
    }

    // 复杂查询
    public Flux<UserDTO> findUsers(int page, int size, String search) {
        Query query = Query.empty()
                          .limit(size)
                          .offset((long) page * size)
                          .sort(Sort.by(Sort.Direction.DESC, "createTime"));

        if (search != null && !search.isEmpty()) {
            query = query.matching(Criteria.where("name").like("%" + search + "%")
                                          .or("email").like("%" + search + "%"));
        }

        return entityTemplate.select(User.class)
                            .matching(query)
                            .all()
                            .map(this::convertToDTO)
                            .doOnNext(user -> System.out.println("查询到用户: " + user.getName()));
    }

    // 事务处理
    @Transactional
    public Mono<UserDTO> createUser(CreateUserRequest request) {
        return Mono.fromCallable(() -> convertToEntity(request))
                  .flatMap(user -> 
                      userRepository.existsByEmail(user.getEmail())
                                   .flatMap(exists -> {
                                       if (exists) {
                                           return Mono.error(new DuplicateEmailException("邮箱已存在"));
                                       }
                                       return userRepository.save(user);
                                   })
                  )
                  .map(this::convertToDTO)
                  .doOnNext(userDTO -> eventPublisher.publishEvent(new UserCreatedEvent(userDTO.getId())))
                  .doOnNext(user -> System.out.println("创建用户: " + user.getName()));
    }

    // 批量操作
    public Flux<UserDTO> createUsers(List<CreateUserRequest> requests) {
        return Flux.fromIterable(requests)
                  .map(this::convertToEntity)
                  .collectList()
                  .flatMapMany(users -> userRepository.saveAll(users))
                  .map(this::convertToDTO)
                  .doOnNext(user -> eventPublisher.publishEvent(new UserCreatedEvent(user.getId())));
    }

    // 流式处理
    public Flux<UserActivityDTO> getUserActivityStream() {
        return Flux.interval(Duration.ofSeconds(1))
                  .flatMap(tick -> userRepository.findRecentActivities(Instant.now().minus(Duration.ofMinutes(1))))
                  .map(this::convertToActivityDTO)
                  .distinct(UserActivityDTO::getUserId)
                  .share(); // 共享流，避免多个订阅者重复执行
    }

    // 文件处理
    public Mono<String> uploadAvatar(String userId, FilePart filePart) {
        return validateImageFile(filePart)
            .then(saveFile(filePart))
            .flatMap(fileName -> updateUserAvatar(userId, fileName))
            .doOnNext(avatarUrl -> System.out.println("用户 " + userId + " 上传头像: " + avatarUrl));
    }

    private Mono<Void> validateImageFile(FilePart filePart) {
        return Mono.fromRunnable(() -> {
            String filename = filePart.filename();
            if (filename == null || (!filename.endsWith(".jpg") && !filename.endsWith(".png"))) {
                throw new IllegalArgumentException("只支持 JPG 和 PNG 格式");
            }
        });
    }

    private Mono<String> saveFile(FilePart filePart) {
        String fileName = UUID.randomUUID() + "_" + filePart.filename();
        Path filePath = Paths.get("uploads", fileName);
        
        return filePart.transferTo(filePath)
                      .thenReturn(fileName);
    }

    private Mono<String> updateUserAvatar(String userId, String fileName) {
        String avatarUrl = "/api/files/" + fileName;
        
        return userRepository.findById(userId)
                           .flatMap(user -> {
                               user.setAvatarUrl(avatarUrl);
                               return userRepository.save(user);
                           })
                           .thenReturn(avatarUrl);
    }

    // 健康检查
    public Mono<String> getHealthStatus() {
        return userRepository.count()
                           .map(count -> count > 0 ? "UP" : "DOWN")
                           .onErrorReturn("DOWN");
    }

    // 转换方法
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                     .id(user.getId())
                     .name(user.getName())
                     .email(user.getEmail())
                     .avatarUrl(user.getAvatarUrl())
                     .createTime(user.getCreateTime())
                     .build();
    }

    private User convertToEntity(CreateUserRequest request) {
        return User.builder()
                  .name(request.getName())
                  .email(request.getEmail())
                  .createTime(Instant.now())
                  .build();
    }

    private UserActivityDTO convertToActivityDTO(UserActivity activity) {
        return UserActivityDTO.builder()
                             .id(activity.getId())
                             .userId(activity.getUserId())
                             .action(activity.getAction())
                             .timestamp(activity.getTimestamp())
                             .build();
    }
}
```

## ⚡ 函数式端点

### 1. Handler 函数实现

```java
@Component
public class UserHandler {

    private final ReactiveUserService userService;
    private final Validator validator;

    public UserHandler(ReactiveUserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    // 获取所有用户
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        int page = request.queryParam("page")
                         .map(Integer::parseInt)
                         .orElse(0);
        int size = request.queryParam("size")
                         .map(Integer::parseInt)
                         .orElse(10);
        String search = request.queryParam("search").orElse(null);

        Flux<UserDTO> users = userService.findUsers(page, size, search);

        return ServerResponse.ok()
                           .contentType(MediaType.APPLICATION_JSON)
                           .body(users, UserDTO.class)
                           .doOnNext(response -> System.out.println("返回用户列表"));
    }

    // 获取单个用户
    public Mono<ServerResponse> getUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return userService.findById(id)
                         .flatMap(user -> ServerResponse.ok().bodyValue(user))
                         .switchIfEmpty(ServerResponse.notFound().build())
                         .doOnNext(response -> System.out.println("返回用户: " + id));
    }

    // 创建用户
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                     .doOnNext(this::validateRequest)
                     .flatMap(userService::createUser)
                     .flatMap(user -> ServerResponse.status(HttpStatus.CREATED)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .bodyValue(user))
                     .onErrorResume(ValidationException.class, 
                         error -> ServerResponse.badRequest().bodyValue(error.getMessage()))
                     .onErrorResume(DuplicateEmailException.class,
                         error -> ServerResponse.status(HttpStatus.CONFLICT).bodyValue(error.getMessage()));
    }

    // 更新用户
    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return request.bodyToMono(UpdateUserRequest.class)
                     .doOnNext(this::validateUpdateRequest)
                     .flatMap(updateRequest -> userService.updateUser(id, updateRequest))
                     .flatMap(user -> ServerResponse.ok().bodyValue(user))
                     .switchIfEmpty(ServerResponse.notFound().build());
    }

    // 删除用户
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return userService.deleteById(id)
                         .then(ServerResponse.noContent().build())
                         .onErrorResume(UserNotFoundException.class,
                             error -> ServerResponse.notFound().build());
    }

    // 流式响应
    public Mono<ServerResponse> streamUsers(ServerRequest request) {
        Flux<UserDTO> userStream = userService.getUserActivityStream()
                                             .map(activity -> userService.findById(activity.getUserId()))
                                             .flatMap(userMono -> userMono)
                                             .distinct(UserDTO::getId);

        return ServerResponse.ok()
                           .contentType(MediaType.TEXT_EVENT_STREAM)
                           .body(userStream, UserDTO.class);
    }

    // 文件上传处理
    public Mono<ServerResponse> uploadFile(ServerRequest request) {
        String userId = request.pathVariable("userId");
        
        return request.multipartData()
                     .flatMap(multipartData -> {
                         Part filePart = multipartData.getFirst("file");
                         if (filePart instanceof FilePart) {
                             return userService.uploadAvatar(userId, (FilePart) filePart);
                         }
                         return Mono.error(new IllegalArgumentException("没有找到文件"));
                     })
                     .flatMap(avatarUrl -> ServerResponse.ok().bodyValue(Map.of("avatarUrl", avatarUrl)))
                     .onErrorResume(error -> ServerResponse.badRequest().bodyValue(error.getMessage()));
    }

    // 服务器推送事件
    public Mono<ServerResponse> sseEndpoint(ServerRequest request) {
        String userId = request.queryParam("userId").orElse(null);
        
        Flux<ServerSentEvent<Object>> eventStream = userService.getUserUpdateStream()
            .filter(event -> userId == null || userId.equals(event.getUserId()))
            .map(event -> ServerSentEvent.builder()
                                        .id(String.valueOf(event.getId()))
                                        .event("user-update")
                                        .data(event)
                                        .build())
            .mergeWith(
                Flux.interval(Duration.ofSeconds(30))
                   .map(tick -> ServerSentEvent.builder()
                                              .event("heartbeat")
                                              .data("ping")
                                              .build())
            );

        return ServerResponse.ok()
                           .contentType(MediaType.TEXT_EVENT_STREAM)
                           .body(eventStream, ServerSentEvent.class);
    }

    // 批量操作
    public Mono<ServerResponse> batchOperation(ServerRequest request) {
        return request.bodyToFlux(BatchUserRequest.class)
                     .collectList()
                     .flatMap(requests -> {
                         Flux<UserDTO> results = Flux.fromIterable(requests)
                             .flatMap(req -> {
                                 switch (req.getOperation()) {
                                     case "CREATE":
                                         return userService.createUser(req.getCreateRequest());
                                     case "UPDATE":
                                         return userService.updateUser(req.getUserId(), req.getUpdateRequest());
                                     case "DELETE":
                                         return userService.deleteById(req.getUserId()).then(Mono.empty());
                                     default:
                                         return Mono.error(new IllegalArgumentException("不支持的操作: " + req.getOperation()));
                                 }
                             })
                             .onErrorContinue((error, item) -> 
                                 System.err.println("批量操作失败: " + error.getMessage()));
                         
                         return ServerResponse.ok().body(results, UserDTO.class);
                     });
    }

    // 验证方法
    private void validateRequest(CreateUserRequest request) {
        Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                                     .map(ConstraintViolation::getMessage)
                                     .collect(Collectors.joining(", "));
            throw new ValidationException(message);
        }
    }

    private void validateUpdateRequest(UpdateUserRequest request) {
        Set<ConstraintViolation<UpdateUserRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                                     .map(ConstraintViolation::getMessage)
                                     .collect(Collectors.joining(", "));
            throw new ValidationException(message);
        }
    }
}
```

### 2. 复杂路由配置

```java
@Configuration
public class RouterConfiguration {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler userHandler) {
        return RouterFunctions
            .nest(path("/api/users"),
                RouterFunctions
                    .route(GET(""), userHandler::getAllUsers)
                    .andRoute(GET("/{id}"), userHandler::getUser)
                    .andRoute(POST(""), userHandler::createUser)
                    .andRoute(PUT("/{id}"), userHandler::updateUser)
                    .andRoute(DELETE("/{id}"), userHandler::deleteUser)
                    .andRoute(GET("/stream"), userHandler::streamUsers)
                    .andRoute(POST("/{userId}/avatar"), userHandler::uploadFile)
                    .andRoute(GET("/sse"), userHandler::sseEndpoint)
                    .andRoute(POST("/batch"), userHandler::batchOperation)
            )
            .andNest(path("/api/admin"),
                RouterFunctions
                    .route(GET("/users").and(accept(MediaType.APPLICATION_JSON)), userHandler::getAllUsers)
                    .filter(authenticationFilter())
                    .filter(authorizationFilter("ADMIN"))
            );
    }

    @Bean
    public RouterFunction<ServerResponse> fileRoutes(FileHandler fileHandler) {
        return RouterFunctions
            .nest(path("/api/files"),
                RouterFunctions
                    .route(POST("/upload"), fileHandler::uploadFile)
                    .andRoute(GET("/{filename}"), fileHandler::downloadFile)
                    .andRoute(DELETE("/{filename}"), fileHandler::deleteFile)
                    .filter(rateLimitFilter())
            );
    }

    @Bean
    public RouterFunction<ServerResponse> healthRoutes(HealthHandler healthHandler) {
        return RouterFunctions
            .route(GET("/health"), healthHandler::health)
            .andRoute(GET("/health/detailed"), healthHandler::detailedHealth)
            .andRoute(GET("/metrics"), healthHandler::metrics);
    }

    // 认证过滤器
    private HandlerFilterFunction<ServerResponse, ServerResponse> authenticationFilter() {
        return (request, next) -> {
            String token = request.headers().firstHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            return authService.validateToken(token.substring(7))
                             .flatMap(valid -> {
                                 if (valid) {
                                     return next.handle(request);
                                 } else {
                                     return ServerResponse.status(HttpStatus.UNAUTHORIZED).build();
                                 }
                             });
        };
    }

    // 授权过滤器
    private HandlerFilterFunction<ServerResponse, ServerResponse> authorizationFilter(String requiredRole) {
        return (request, next) -> {
            String token = request.headers().firstHeader("Authorization");
            
            return authService.getUserRole(token.substring(7))
                             .flatMap(role -> {
                                 if (requiredRole.equals(role)) {
                                     return next.handle(request);
                                 } else {
                                     return ServerResponse.status(HttpStatus.FORBIDDEN).build();
                                 }
                             });
        };
    }

    // 限流过滤器
    private HandlerFilterFunction<ServerResponse, ServerResponse> rateLimitFilter() {
        return (request, next) -> {
            String clientIP = getClientIP(request);
            
            return rateLimitService.isAllowed(clientIP)
                                  .flatMap(allowed -> {
                                      if (allowed) {
                                          return next.handle(request);
                                      } else {
                                          return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS).build();
                                      }
                                  });
        };
    }

    private String getClientIP(ServerRequest request) {
        return request.headers().firstHeader("X-Forwarded-For");
    }
}
```

## 🔧 WebClient 响应式客户端

### 1. WebClient 配置和基础使用

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                       .codecs(configurer -> {
                           configurer.defaultCodecs().maxInMemorySize(1024 * 1024); // 1MB
                           configurer.defaultCodecs().enableLoggingRequestDetails(true);
                       })
                       .defaultHeader(HttpHeaders.USER_AGENT, "Spring-WebFlux-Client/1.0")
                       .defaultCookie("client-id", "spring-webflux");
    }

    @Bean
    public WebClient externalApiClient(WebClient.Builder builder) {
        return builder
            .baseUrl("https://api.external-service.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .filter(logRequest())
            .filter(logResponse())
            .filter(retryFilter())
            .build();
    }

    // 请求日志过滤器
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("请求: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> 
                values.forEach(value -> System.out.println(name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }

    // 响应日志过滤器
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("响应状态: " + clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }

    // 重试过滤器
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return Mono.error(new RuntimeException("服务器错误"));
            }
            return Mono.just(clientResponse);
        });
    }
}

@Service
public class ExternalApiService {

    private final WebClient webClient;

    public ExternalApiService(WebClient externalApiClient) {
        this.webClient = externalApiClient;
    }

    // GET 请求
    public Mono<UserInfo> getUserInfo(String userId) {
        return webClient.get()
                       .uri("/users/{id}", userId)
                       .retrieve()
                       .onStatus(HttpStatus::is4xxClientError, response -> {
                           if (response.statusCode() == HttpStatus.NOT_FOUND) {
                               return Mono.error(new UserNotFoundException("用户不存在: " + userId));
                           }
                           return Mono.error(new RuntimeException("客户端错误: " + response.statusCode()));
                       })
                       .bodyToMono(UserInfo.class)
                       .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                       .timeout(Duration.ofSeconds(5))
                       .doOnNext(userInfo -> System.out.println("获取用户信息: " + userInfo.getName()));
    }

    // POST 请求
    public Mono<CreateUserResponse> createUser(CreateUserRequest request) {
        return webClient.post()
                       .uri("/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .bodyValue(request)
                       .retrieve()
                       .bodyToMono(CreateUserResponse.class)
                       .onErrorMap(WebClientResponseException.class, ex -> {
                           if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                               return new DuplicateEmailException("邮箱已存在");
                           }
                           return new RuntimeException("创建用户失败: " + ex.getMessage());
                       });
    }

    // 流式响应处理
    public Flux<UserEvent> streamUserEvents() {
        return webClient.get()
                       .uri("/users/events/stream")
                       .accept(MediaType.TEXT_EVENT_STREAM)
                       .retrieve()
                       .bodyToFlux(UserEvent.class)
                       .doOnNext(event -> System.out.println("接收到用户事件: " + event.getType()))
                       .doOnError(error -> System.err.println("流连接错误: " + error.getMessage()))
                       .retry(3);
    }

    // 文件上传
    public Mono<UploadResponse> uploadFile(String userId, Resource fileResource) {
        return webClient.post()
                       .uri("/users/{id}/files", userId)
                       .contentType(MediaType.MULTIPART_FORM_DATA)
                       .body(BodyInserters.fromMultipartData("file", fileResource))
                       .retrieve()
                       .bodyToMono(UploadResponse.class);
    }

    // 批量请求
    public Flux<UserInfo> batchGetUsers(List<String> userIds) {
        return Flux.fromIterable(userIds)
                  .flatMap(this::getUserInfo, 5) // 并发度为5
                  .onErrorContinue((error, userId) -> 
                      System.err.println("获取用户 " + userId + " 失败: " + error.getMessage()));
    }

    // 条件请求
    public Mono<UserInfo> getUserInfoIfModified(String userId, String etag) {
        return webClient.get()
                       .uri("/users/{id}", userId)
                       .header(HttpHeaders.IF_NONE_MATCH, etag)
                       .retrieve()
                       .onStatus(HttpStatus.NOT_MODIFIED::equals, response -> Mono.empty())
                       .bodyToMono(UserInfo.class);
    }
}
```

### 2. 高级 WebClient 使用场景

```java
@Service
public class AdvancedWebClientService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public AdvancedWebClientService(WebClient.Builder webClientBuilder,
                                  ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.webClient = webClientBuilder
            .filter(cacheFilter())
            .filter(circuitBreakerFilter())
            .build();
    }

    // 并行请求聚合
    public Mono<UserProfile> getUserProfile(String userId) {
        Mono<UserInfo> userInfo = getUserBasicInfo(userId);
        Mono<List<Order>> userOrders = getUserOrders(userId);
        Mono<UserPreferences> userPreferences = getUserPreferences(userId);

        return Mono.zip(userInfo, userOrders, userPreferences)
                  .map(tuple -> UserProfile.builder()
                                          .userInfo(tuple.getT1())
                                          .orders(tuple.getT2())
                                          .preferences(tuple.getT3())
                                          .build())
                  .timeout(Duration.ofSeconds(10));
    }

    // 链式请求
    public Mono<OrderDetails> getOrderWithUserInfo(String orderId) {
        return getOrder(orderId)
            .flatMap(order -> 
                getUserBasicInfo(order.getUserId())
                    .map(userInfo -> OrderDetails.builder()
                                                .order(order)
                                                .userInfo(userInfo)
                                                .build())
            );
    }

    // 条件请求链
    public Mono<ProcessResult> processUserAction(String userId, String action) {
        return getUserBasicInfo(userId)
            .flatMap(user -> {
                if ("premium".equals(user.getAccountType())) {
                    return processPremiumAction(userId, action);
                } else {
                    return processStandardAction(userId, action);
                }
            })
            .onErrorResume(error -> {
                System.err.println("处理用户操作失败: " + error.getMessage());
                return Mono.just(ProcessResult.failed("操作失败"));
            });
    }

    // 缓存过滤器
    private ExchangeFilterFunction cacheFilter() {
        return (request, next) -> {
            String cacheKey = generateCacheKey(request);
            
            return redisTemplate.opsForValue().get(cacheKey)
                               .cast(String.class)
                               .map(cachedResponse -> {
                                   // 从缓存返回响应（简化实现）
                                   return ClientResponse.create(HttpStatus.OK)
                                                       .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                       .body(cachedResponse)
                                                       .build();
                               })
                               .switchIfEmpty(
                                   next.exchange(request)
                                       .flatMap(response -> {
                                           if (response.statusCode().is2xxSuccessful()) {
                                               return response.bodyToMono(String.class)
                                                            .flatMap(body -> 
                                                                redisTemplate.opsForValue()
                                                                           .set(cacheKey, body, Duration.ofMinutes(5))
                                                                           .thenReturn(response)
                                                            );
                                           }
                                           return Mono.just(response);
                                       })
                               );
        };
    }

    // 断路器过滤器
    private ExchangeFilterFunction circuitBreakerFilter() {
        return (request, next) -> {
            String serviceKey = extractServiceKey(request);
            
            return checkCircuitBreakerState(serviceKey)
                .flatMap(isOpen -> {
                    if (isOpen) {
                        return Mono.error(new CircuitBreakerOpenException("断路器开启"));
                    }
                    
                    return next.exchange(request)
                              .doOnNext(response -> recordSuccess(serviceKey))
                              .doOnError(error -> recordFailure(serviceKey));
                });
        };
    }

    // 辅助方法
    private Mono<UserInfo> getUserBasicInfo(String userId) {
        return webClient.get()
                       .uri("/users/{id}", userId)
                       .retrieve()
                       .bodyToMono(UserInfo.class);
    }

    private Mono<List<Order>> getUserOrders(String userId) {
        return webClient.get()
                       .uri("/users/{id}/orders", userId)
                       .retrieve()
                       .bodyToFlux(Order.class)
                       .collectList();
    }

    private Mono<UserPreferences> getUserPreferences(String userId) {
        return webClient.get()
                       .uri("/users/{id}/preferences", userId)
                       .retrieve()
                       .bodyToMono(UserPreferences.class);
    }

    private Mono<Order> getOrder(String orderId) {
        return webClient.get()
                       .uri("/orders/{id}", orderId)
                       .retrieve()
                       .bodyToMono(Order.class);
    }

    private Mono<ProcessResult> processPremiumAction(String userId, String action) {
        return webClient.post()
                       .uri("/premium/actions")
                       .bodyValue(Map.of("userId", userId, "action", action))
                       .retrieve()
                       .bodyToMono(ProcessResult.class);
    }

    private Mono<ProcessResult> processStandardAction(String userId, String action) {
        return webClient.post()
                       .uri("/standard/actions")
                       .bodyValue(Map.of("userId", userId, "action", action))
                       .retrieve()
                       .bodyToMono(ProcessResult.class);
    }

    private String generateCacheKey(ClientRequest request) {
        return "cache:" + request.method() + ":" + request.url().toString().hashCode();
    }

    private String extractServiceKey(ClientRequest request) {
        return request.url().getHost();
    }

    private Mono<Boolean> checkCircuitBreakerState(String serviceKey) {
        return redisTemplate.opsForValue()
                           .get("cb:" + serviceKey + ":state")
                           .map("OPEN"::equals)
                           .defaultIfEmpty(false);
    }

    private void recordSuccess(String serviceKey) {
        // 记录成功调用
        redisTemplate.opsForValue().set("cb:" + serviceKey + ":state", "CLOSED").subscribe();
    }

    private void recordFailure(String serviceKey) {
        // 记录失败调用，可能触发断路器
        redisTemplate.opsForValue().set("cb:" + serviceKey + ":state", "OPEN").subscribe();
    }
}
```

## 🚦 错误处理和异常管理

### 1. 全局异常处理

```java
@Component
@Order(-2) // 高优先级
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalWebExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        // 设置响应头
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        HttpStatus status;
        String message;
        String errorCode;

        if (ex instanceof ValidationException) {
            status = HttpStatus.BAD_REQUEST;
            message = ex.getMessage();
            errorCode = "VALIDATION_ERROR";
        } else if (ex instanceof UserNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = ex.getMessage();
            errorCode = "USER_NOT_FOUND";
        } else if (ex instanceof DuplicateEmailException) {
            status = HttpStatus.CONFLICT;
            message = ex.getMessage();
            errorCode = "DUPLICATE_EMAIL";
        } else if (ex instanceof SecurityException) {
            status = HttpStatus.FORBIDDEN;
            message = "访问被拒绝";
            errorCode = "ACCESS_DENIED";
        } else if (ex instanceof TimeoutException) {
            status = HttpStatus.REQUEST_TIMEOUT;
            message = "请求超时";
            errorCode = "REQUEST_TIMEOUT";
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "服务器内部错误";
            errorCode = "INTERNAL_ERROR";
            logger.error("未处理的异常", ex);
        }

        response.setStatusCode(status);

        // 构建错误响应
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .timestamp(Instant.now())
                                                  .status(status.value())
                                                  .error(status.getReasonPhrase())
                                                  .message(message)
                                                  .code(errorCode)
                                                  .path(exchange.getRequest().getPath().value())
                                                  .build();

        String errorJson;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            errorJson = objectMapper.writeValueAsString(errorResponse);
        } catch (Exception e) {
            errorJson = "{\"error\":\"序列化错误响应失败\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}

// 控制器级别异常处理
@RestControllerAdvice
public class ReactiveControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveControllerAdvice.class);

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationErrors(WebExchangeBindException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        Map<String, String> errors = fieldErrors.stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing
            ));

        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                                                                      .message("验证失败")
                                                                      .fieldErrors(errors)
                                                                      .timestamp(Instant.now())
                                                                      .build();

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInputError(ServerWebInputException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message("输入参数错误: " + ex.getReason())
                                                  .code("INPUT_ERROR")
                                                  .timestamp(Instant.now())
                                                  .build();

        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message(ex.getReason())
                                                  .code("RESPONSE_STATUS_ERROR")
                                                  .timestamp(Instant.now())
                                                  .build();

        return Mono.just(ResponseEntity.status(ex.getStatus()).body(errorResponse));
    }
}
```

### 2. 自定义异常和错误码

```java
// 自定义异常基类
public abstract class BusinessException extends RuntimeException {
    private final String errorCode;
    
    protected BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    protected BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}

// 具体业务异常
public class UserNotFoundException extends BusinessException {
    public UserNotFoundException(String userId) {
        super("USER_NOT_FOUND", "用户不存在: " + userId);
    }
}

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException(String email) {
        super("DUPLICATE_EMAIL", "邮箱已存在: " + email);
    }
}

public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}

public class CircuitBreakerOpenException extends BusinessException {
    public CircuitBreakerOpenException(String service) {
        super("CIRCUIT_BREAKER_OPEN", "服务 " + service + " 断路器开启");
    }
}

// 错误响应类
@Data
@Builder
public class ErrorResponse {
    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String code;
    private String path;
    private String requestId;
}

@Data
@Builder
public class ValidationErrorResponse {
    private String message;
    private Map<String, String> fieldErrors;
    private Instant timestamp;
    private String requestId;
}
```

## 📊 性能优化和监控

### 1. 性能优化策略

```java
@Configuration
public class WebFluxPerformanceConfig {

    // 自定义调度器
    @Bean
    public Scheduler customScheduler() {
        return Schedulers.newBoundedElastic(
            20,     // 最大线程数
            1000,   // 队列容量
            "custom-scheduler"
        );
    }

    // 配置 Netty 服务器
    @Bean
    public NettyReactiveWebServerFactory nettyReactiveWebServerFactory() {
        NettyReactiveWebServerFactory factory = new NettyReactiveWebServerFactory();
        
        factory.addServerCustomizers(httpServer -> 
            httpServer.option(ChannelOption.SO_KEEPALIVE, true)
                     .option(ChannelOption.SO_BACKLOG, 1024)
                     .childOption(ChannelOption.TCP_NODELAY, true)
                     .idleTimeout(Duration.ofMinutes(5))
                     .accessLog(true)
        );
        
        return factory;
    }

    // 自定义编解码器
    @Bean
    public CodecCustomizer codecCustomizer() {
        return configurer -> {
            configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
            configurer.defaultCodecs().enableLoggingRequestDetails(true);
            
            // Jackson 配置
            configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper()));
            configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper()));
        };
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }
}

// 性能监控
@Component
public class PerformanceMonitor {

    private final MeterRegistry meterRegistry;
    private final Timer requestTimer;
    private final Counter requestCounter;
    private final Gauge activeConnections;

    public PerformanceMonitor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestTimer = Timer.builder("webflux.request.duration")
                                .description("WebFlux request duration")
                                .register(meterRegistry);
        this.requestCounter = Counter.builder("webflux.request.count")
                                   .description("WebFlux request count")
                                   .register(meterRegistry);
        this.activeConnections = Gauge.builder("webflux.connections.active")
                                     .description("Active WebFlux connections")
                                     .register(meterRegistry, this, PerformanceMonitor::getActiveConnections);
    }

    public Timer.Sample startTimer() {
        requestCounter.increment();
        return Timer.start(meterRegistry);
    }

    public void recordRequest(Timer.Sample sample, String method, String uri, int statusCode) {
        sample.stop(Timer.builder("webflux.request.duration")
                        .tag("method", method)
                        .tag("uri", uri)
                        .tag("status", String.valueOf(statusCode))
                        .register(meterRegistry));
    }

    private double getActiveConnections() {
        // 获取活跃连接数的实现
        return 0.0;
    }
}

// 缓存配置
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public ReactiveRedisTemplate<String, Object> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, Object> builder = 
            RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        
        RedisSerializationContext<String, Object> context = builder
            .value(new GenericJackson2JsonRedisSerializer())
            .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public CacheManager cacheManager(ReactiveRedisConnectionFactory factory) {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                               .cacheDefaults(cacheConfig)
                               .build();
    }
}
```

### 2. 监控和调试

```java
// 请求监控过滤器
@Component
public class RequestMonitoringFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(RequestMonitoringFilter.class);
    private final PerformanceMonitor performanceMonitor;

    public RequestMonitoringFilter(PerformanceMonitor performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Timer.Sample sample = performanceMonitor.startTimer();
        ServerHttpRequest request = exchange.getRequest();
        
        // 添加请求 ID
        String requestId = UUID.randomUUID().toString();
        exchange.getAttributes().put("requestId", requestId);
        
        // 添加开始时间
        exchange.getAttributes().put("startTime", System.currentTimeMillis());
        
        return chain.filter(exchange)
                   .doOnSuccess(result -> {
                       long duration = System.currentTimeMillis() - 
                                     (Long) exchange.getAttributes().get("startTime");
                       
                       ServerHttpResponse response = exchange.getResponse();
                       performanceMonitor.recordRequest(
                           sample,
                           request.getMethod().name(),
                           request.getPath().value(),
                           response.getStatusCode().value()
                       );
                       
                       logger.info("请求完成: {} {} - {}ms - {} - RequestId: {}",
                           request.getMethod(),
                           request.getPath(),
                           duration,
                           response.getStatusCode(),
                           requestId);
                   })
                   .doOnError(error -> {
                       long duration = System.currentTimeMillis() - 
                                     (Long) exchange.getAttributes().get("startTime");
                       
                       logger.error("请求异常: {} {} - {}ms - RequestId: {} - Error: {}",
                           request.getMethod(),
                           request.getPath(),
                           duration,
                           requestId,
                           error.getMessage());
                   });
    }
}

// 健康检查端点
@RestController
@RequestMapping("/actuator")
public class HealthController {

    private final ReactiveUserService userService;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    public HealthController(ReactiveUserService userService,
                          ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return Mono.zip(
            checkDatabaseHealth(),
            checkRedisHealth(),
            checkServiceHealth()
        ).map(tuple -> {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", Instant.now());
            health.put("database", tuple.getT1());
            health.put("redis", tuple.getT2());
            health.put("service", tuple.getT3());
            
            boolean allHealthy = Stream.of(tuple.getT1(), tuple.getT2(), tuple.getT3())
                                     .map(status -> "UP".equals(status.get("status")))
                                     .allMatch(Boolean::booleanValue);
            
            if (!allHealthy) {
                health.put("status", "DOWN");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
            }
            
            return ResponseEntity.ok(health);
        });
    }

    private Mono<Map<String, Object>> checkDatabaseHealth() {
        return userService.getHealthStatus()
                         .map(status -> Map.of("status", status, "type", "database"))
                         .onErrorReturn(Map.of("status", "DOWN", "type", "database"));
    }

    private Mono<Map<String, Object>> checkRedisHealth() {
        return redisTemplate.hasKey("health-check")
                           .map(exists -> Map.of("status", "UP", "type", "redis"))
                           .onErrorReturn(Map.of("status", "DOWN", "type", "redis"));
    }

    private Mono<Map<String, Object>> checkServiceHealth() {
        return Mono.just(Map.of("status", "UP", "type", "service"));
    }
}
```

## 📝 小结

Spring WebFlux 提供了完整的响应式 Web 编程解决方案：

### 核心优势总结

- **高并发处理** - 基于事件循环的非阻塞 I/O 模型
- **函数式编程** - 支持函数式端点和响应式流处理
- **背压支持** - 内置背压控制机制，防止内存溢出
- **灵活部署** - 支持 Netty、Undertow、Jetty 等容器

### 架构对比

| 特性 | Spring MVC | Spring WebFlux |
|------|------------|----------------|
| **编程模型** | 阻塞式 | 非阻塞响应式 |
| **并发模型** | 每请求一线程 | 事件循环 |
| **适用场景** | I/O 密集型应用 | 高并发、流处理 |
| **学习曲线** | 相对平缓 | 较陡峭 |

### 最佳实践要点

1. **适用场景判断** - 高并发、I/O 密集型应用选择 WebFlux
2. **错误处理** - 完善的响应式错误处理和恢复机制
3. **性能调优** - 合理配置调度器和缓冲区大小
4. **监控指标** - 建立完善的性能监控体系
5. **背压控制** - 合理使用限流和缓冲策略

Spring WebFlux 特别适合构建高并发的微服务、实时数据处理系统和流式应用，结合响应式数据库和消息队列，能够构建完全非阻塞的应用架构。
