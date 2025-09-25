---
title: Spring Web 测试与集成技术详解
description: Spring Web 测试与集成技术详解
tags: [Spring Test, WebSocket, Integration]
category: Spring
date: 2025-09-25
---

# Spring Web 测试与集成技术详解

## 🎯 概述

Spring Web 测试与集成技术是确保 Web 应用质量的重要组成部分。根据 [Spring Framework 官方文档](https://docs.spring.io/spring-framework/reference/web.html)，Spring 提供了完整的测试框架，包括单元测试、集成测试、WebSocket 支持以及各种外部系统集成方案。本文将深入探讨这些技术的核心概念、实现方式和最佳实践。

## 🧪 Spring Web 测试框架

### 1. 测试架构概览

```
Spring 测试层次架构

单元测试层 (Unit Tests)
├── @WebMvcTest (MVC层测试)
├── @WebFluxTest (WebFlux层测试)
├── @JsonTest (JSON序列化测试)
└── @DataJpaTest (数据层测试)
    ↓
集成测试层 (Integration Tests)
├── @SpringBootTest (完整集成测试)
├── MockMvc (模拟MVC测试)
├── WebTestClient (响应式测试客户端)
└── TestRestTemplate (REST测试模板)
    ↓
端到端测试层 (E2E Tests)
├── Selenium WebDriver
├── TestContainers
└── WireMock (外部服务模拟)
```

### 2. MockMvc 测试详解

```java
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // GET 请求测试
    @Test
    @DisplayName("获取用户信息 - 成功")
    public void getUserById_Success() throws Exception {
        // Given
        Long userId = 1L;
        UserDTO user = UserDTO.builder()
                             .id(userId)
                             .name("张三")
                             .email("zhangsan@example.com")
                             .build();

        when(userService.findById(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                       .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON))
               .andExpect(jsonPath("$.id").value(userId))
               .andExpect(jsonPath("$.name").value("张三"))
               .andExpect(jsonPath("$.email").value("zhangsan@example.com"))
               .andDo(print()) // 打印请求和响应详情
               .andDo(document("get-user-by-id")); // Spring REST Docs
    }

    @Test
    @DisplayName("获取用户信息 - 用户不存在")
    public void getUserById_NotFound() throws Exception {
        // Given
        Long userId = 999L;
        when(userService.findById(userId)).thenThrow(new UserNotFoundException("用户不存在"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId))
               .andExpect(status().isNotFound())
               .andExpected(jsonPath("$.message").value("用户不存在"))
               .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // POST 请求测试
    @Test
    @DisplayName("创建用户 - 成功")
    public void createUser_Success() throws Exception {
        // Given
        CreateUserRequest request = CreateUserRequest.builder()
                                                    .name("李四")
                                                    .email("lisi@example.com")
                                                    .age(25)
                                                    .build();

        UserDTO savedUser = UserDTO.builder()
                                  .id(2L)
                                  .name("李四")
                                  .email("lisi@example.com")
                                  .build();

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(2L))
               .andExpect(jsonPath("$.name").value("李四"))
               .andExpect(header().exists("Location"));

        verify(userService).createUser(argThat(req -> 
            "李四".equals(req.getName()) && "lisi@example.com".equals(req.getEmail())
        ));
    }

    // 验证失败测试
    @Test
    @DisplayName("创建用户 - 验证失败")
    public void createUser_ValidationError() throws Exception {
        // Given
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                                                          .name("") // 空名称
                                                          .email("invalid-email") // 无效邮箱
                                                          .age(-1) // 无效年龄
                                                          .build();

        // When & Then
        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(invalidRequest)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.fieldErrors.name").exists())
               .andExpect(jsonPath("$.fieldErrors.email").exists())
               .andExpect(jsonPath("$.fieldErrors.age").exists());

        verify(userService, never()).createUser(any());
    }

    // 文件上传测试
    @Test
    @DisplayName("上传用户头像 - 成功")
    public void uploadAvatar_Success() throws Exception {
        // Given
        Long userId = 1L;
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image content".getBytes()
        );

        String avatarUrl = "/api/files/avatar.jpg";
        when(userService.uploadAvatar(userId, file)).thenReturn(avatarUrl);

        // When & Then
        mockMvc.perform(multipart("/api/users/{id}/avatar", userId)
                       .file(file))
               .andExpect(status().isOk())
               .andExpect(content().string(avatarUrl));

        verify(userService).uploadAvatar(eq(userId), any(MultipartFile.class));
    }

    // 权限测试
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("删除用户 - 管理员权限")
    public void deleteUser_AdminRole() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteById(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
               .andExpect(status().isNoContent());

        verify(userService).deleteById(userId);
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("删除用户 - 普通用户权限不足")
    public void deleteUser_UserRole_Forbidden() throws Exception {
        // Given
        Long userId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
               .andExpect(status().isForbidden());

        verify(userService, never()).deleteById(any());
    }

    // 参数化测试
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "a", "ab"})
    @DisplayName("创建用户 - 姓名长度验证")
    public void createUser_NameValidation(String name) throws Exception {
        CreateUserRequest request = CreateUserRequest.builder()
                                                    .name(name)
                                                    .email("test@example.com")
                                                    .age(25)
                                                    .build();

        mockMvc.perform(post("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.fieldErrors.name").exists());
    }
}
```

### 3. WebTestClient 响应式测试

```java
@WebFluxTest(ReactiveUserController.class)
public class ReactiveUserControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveUserService userService;

    // 单个资源测试
    @Test
    @DisplayName("获取用户 - 响应式测试")
    public void getUser_Reactive() {
        // Given
        String userId = "user123";
        UserDTO user = UserDTO.builder()
                             .id(userId)
                             .name("王五")
                             .email("wangwu@example.com")
                             .build();

        when(userService.findById(userId)).thenReturn(Mono.just(user));

        // When & Then
        webTestClient.get()
                    .uri("/api/reactive/users/{id}", userId)
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBody(UserDTO.class)
                    .value(responseUser -> {
                        assertThat(responseUser.getId()).isEqualTo(userId);
                        assertThat(responseUser.getName()).isEqualTo("王五");
                        assertThat(responseUser.getEmail()).isEqualTo("wangwu@example.com");
                    });

        verify(userService).findById(userId);
    }

    // 流式响应测试
    @Test
    @DisplayName("获取用户列表 - 流式响应")
    public void getUsers_Stream() {
        // Given
        List<UserDTO> users = Arrays.asList(
            UserDTO.builder().id("1").name("用户1").build(),
            UserDTO.builder().id("2").name("用户2").build(),
            UserDTO.builder().id("3").name("用户3").build()
        );

        when(userService.findUsers(0, 10, null))
            .thenReturn(Flux.fromIterable(users));

        // When & Then
        webTestClient.get()
                    .uri("/api/reactive/users")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_JSON)
                    .expectBodyList(UserDTO.class)
                    .hasSize(3)
                    .value(responseUsers -> {
                        assertThat(responseUsers.get(0).getName()).isEqualTo("用户1");
                        assertThat(responseUsers.get(1).getName()).isEqualTo("用户2");
                        assertThat(responseUsers.get(2).getName()).isEqualTo("用户3");
                    });
    }

    // SSE 测试
    @Test
    @DisplayName("服务器推送事件测试")
    public void streamUserActivities_SSE() {
        // Given
        Flux<UserActivityDTO> activities = Flux.just(
            UserActivityDTO.builder().userId("1").action("LOGIN").build(),
            UserActivityDTO.builder().userId("2").action("LOGOUT").build()
        ).delayElements(Duration.ofMillis(100));

        when(userService.getUserActivityStream()).thenReturn(activities);

        // When & Then
        webTestClient.get()
                    .uri("/api/reactive/users/stream")
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                    .returnResult(ServerSentEvent.class)
                    .getResponseBody()
                    .take(2)
                    .collectList()
                    .as(StepVerifier::create)
                    .assertNext(events -> {
                        assertThat(events).hasSize(2);
                        assertThat(events.get(0).event()).isEqualTo("user-activity");
                        assertThat(events.get(1).event()).isEqualTo("user-activity");
                    })
                    .verifyComplete();
    }

    // 错误处理测试
    @Test
    @DisplayName("用户不存在 - 错误处理")
    public void getUser_NotFound() {
        // Given
        String userId = "nonexistent";
        when(userService.findById(userId))
            .thenReturn(Mono.error(new UserNotFoundException("用户不存在")));

        // When & Then
        webTestClient.get()
                    .uri("/api/reactive/users/{id}", userId)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.message").isEqualTo("用户不存在")
                    .jsonPath("$.code").isEqualTo("USER_NOT_FOUND");
    }

    // 背压测试
    @Test
    @DisplayName("背压处理测试")
    public void backpressureHandling() {
        // Given
        Flux<UserDTO> largeUserStream = Flux.range(1, 1000)
                                           .map(i -> UserDTO.builder()
                                                           .id(String.valueOf(i))
                                                           .name("用户" + i)
                                                           .build())
                                           .limitRate(10); // 限制速率

        when(userService.findUsers(anyInt(), anyInt(), any()))
            .thenReturn(largeUserStream);

        // When & Then
        webTestClient.get()
                    .uri("/api/reactive/users?size=1000")
                    .exchange()
                    .expectStatus().isOk()
                    .returnResult(UserDTO.class)
                    .getResponseBody()
                    .take(100) // 只取前100个
                    .collectList()
                    .as(StepVerifier::create)
                    .assertNext(users -> assertThat(users).hasSize(100))
                    .verifyComplete();
    }
}
```

### 4. 集成测试策略

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("完整用户生命周期测试")
    public void userLifecycleIntegrationTest() {
        // 1. 创建用户
        CreateUserRequest createRequest = CreateUserRequest.builder()
                                                          .name("集成测试用户")
                                                          .email("integration@test.com")
                                                          .age(30)
                                                          .build();

        ResponseEntity<UserDTO> createResponse = restTemplate.postForEntity(
            "/api/users",
            createRequest,
            UserDTO.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getName()).isEqualTo("集成测试用户");

        Long userId = createResponse.getBody().getId();

        // 2. 获取用户
        ResponseEntity<UserDTO> getResponse = restTemplate.getForEntity(
            "/api/users/" + userId,
            UserDTO.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("集成测试用户");

        // 3. 更新用户
        UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                                                          .name("更新后的用户")
                                                          .age(31)
                                                          .build();

        ResponseEntity<UserDTO> updateResponse = restTemplate.exchange(
            "/api/users/" + userId,
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            UserDTO.class
        );

        assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResponse.getBody().getName()).isEqualTo("更新后的用户");

        // 4. 验证数据库状态
        Optional<User> userInDb = userRepository.findById(userId);
        assertThat(userInDb).isPresent();
        assertThat(userInDb.get().getName()).isEqualTo("更新后的用户");

        // 5. 删除用户
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
            "/api/users/" + userId,
            HttpMethod.DELETE,
            null,
            Void.class
        );

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 6. 验证用户已删除
        ResponseEntity<String> getDeletedResponse = restTemplate.getForEntity(
            "/api/users/" + userId,
            String.class
        );

        assertThat(getDeletedResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("并发用户创建测试")
    public void concurrentUserCreation() throws InterruptedException {
        int threadCount = 10;
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            new Thread(() -> {
                try {
                    CreateUserRequest request = CreateUserRequest.builder()
                                                                .name("并发用户" + index)
                                                                .email("concurrent" + index + "@test.com")
                                                                .age(25)
                                                                .build();

                    ResponseEntity<UserDTO> response = restTemplate.postForEntity(
                        "/api/users",
                        request,
                        UserDTO.class
                    );

                    if (response.getStatusCode() == HttpStatus.CREATED) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await(10, TimeUnit.SECONDS);

        assertThat(successCount.get()).isEqualTo(threadCount);
        assertThat(errorCount.get()).isEqualTo(0);

        // 验证数据库中确实有这些用户
        List<User> users = userRepository.findAll();
        assertThat(users).hasSize(threadCount);
    }
}
```

## 🔗 WebSocket 技术详解

### 1. WebSocket 配置和端点

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat")
                .setAllowedOrigins("*") // 生产环境应指定具体域名
                .withSockJS(); // 支持SockJS回退

        registry.addHandler(new NotificationWebSocketHandler(), "/ws/notifications")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("http://localhost:3000", "https://yourdomain.com");
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}

// 聊天 WebSocket 处理器
public class ChatWebSocketHandler extends TextWebSocketHandler implements SubProtocolCapable {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();
    
    private final ChatService chatService;
    private final ObjectMapper objectMapper;

    public ChatWebSocketHandler(ChatService chatService, ObjectMapper objectMapper) {
        this.chatService = chatService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserId(session);
        sessions.put(session.getId(), session);
        userSessions.put(userId, session.getId());
        
        logger.info("用户 {} 连接到聊天室，会话ID: {}", userId, session.getId());
        
        // 发送欢迎消息
        ChatMessage welcomeMessage = ChatMessage.builder()
                                               .type(ChatMessage.Type.SYSTEM)
                                               .content("欢迎进入聊天室！")
                                               .timestamp(Instant.now())
                                               .build();
        
        sendMessage(session, welcomeMessage);
        
        // 通知其他用户有新用户加入
        broadcastUserJoined(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            String userId = getUserId(session);
            
            // 设置发送者信息
            chatMessage.setSenderId(userId);
            chatMessage.setTimestamp(Instant.now());
            
            // 处理不同类型的消息
            switch (chatMessage.getType()) {
                case CHAT:
                    handleChatMessage(chatMessage, session);
                    break;
                case PRIVATE:
                    handlePrivateMessage(chatMessage, session);
                    break;
                case TYPING:
                    handleTypingIndicator(chatMessage, session);
                    break;
                default:
                    logger.warn("未知消息类型: {}", chatMessage.getType());
            }
            
        } catch (Exception e) {
            logger.error("处理消息失败", e);
            sendErrorMessage(session, "消息处理失败: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        logger.error("WebSocket传输错误, 会话: {}", session.getId(), exception);
        sessions.remove(session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String userId = getUserId(session);
        sessions.remove(session.getId());
        userSessions.remove(userId);
        
        logger.info("用户 {} 断开连接，状态: {}", userId, closeStatus);
        
        // 通知其他用户有用户离开
        broadcastUserLeft(userId);
    }

    private void handleChatMessage(ChatMessage message, WebSocketSession senderSession) throws Exception {
        // 保存消息到数据库
        ChatMessage savedMessage = chatService.saveMessage(message);
        
        // 广播消息给所有连接的用户
        broadcastMessage(savedMessage, senderSession.getId());
    }

    private void handlePrivateMessage(ChatMessage message, WebSocketSession senderSession) throws Exception {
        String recipientId = message.getRecipientId();
        String recipientSessionId = userSessions.get(recipientId);
        
        if (recipientSessionId != null) {
            WebSocketSession recipientSession = sessions.get(recipientSessionId);
            if (recipientSession != null && recipientSession.isOpen()) {
                sendMessage(recipientSession, message);
                
                // 给发送者发送确认
                ChatMessage confirmation = ChatMessage.builder()
                                                     .type(ChatMessage.Type.SYSTEM)
                                                     .content("私信已发送")
                                                     .build();
                sendMessage(senderSession, confirmation);
            } else {
                sendErrorMessage(senderSession, "用户不在线");
            }
        } else {
            sendErrorMessage(senderSession, "用户不存在");
        }
        
        // 保存私信记录
        chatService.savePrivateMessage(message);
    }

    private void handleTypingIndicator(ChatMessage message, WebSocketSession senderSession) throws Exception {
        // 广播打字指示器给其他用户
        message.setSenderId(getUserId(senderSession));
        broadcastMessage(message, senderSession.getId());
    }

    private void broadcastMessage(ChatMessage message, String excludeSessionId) throws Exception {
        String messageJson = objectMapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(messageJson);
        
        for (WebSocketSession session : sessions.values()) {
            if (!session.getId().equals(excludeSessionId) && session.isOpen()) {
                try {
                    session.sendMessage(textMessage);
                } catch (Exception e) {
                    logger.error("发送广播消息失败, 会话: {}", session.getId(), e);
                }
            }
        }
    }

    private void broadcastUserJoined(String userId, WebSocketSession excludeSession) throws Exception {
        ChatMessage message = ChatMessage.builder()
                                        .type(ChatMessage.Type.USER_JOINED)
                                        .senderId(userId)
                                        .content(userId + " 加入了聊天室")
                                        .timestamp(Instant.now())
                                        .build();
        
        broadcastMessage(message, excludeSession.getId());
    }

    private void broadcastUserLeft(String userId) throws Exception {
        ChatMessage message = ChatMessage.builder()
                                        .type(ChatMessage.Type.USER_LEFT)
                                        .senderId(userId)
                                        .content(userId + " 离开了聊天室")
                                        .timestamp(Instant.now())
                                        .build();
        
        broadcastMessage(message, null);
    }

    private void sendMessage(WebSocketSession session, ChatMessage message) throws Exception {
        if (session.isOpen()) {
            String messageJson = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(messageJson));
        }
    }

    private void sendErrorMessage(WebSocketSession session, String error) throws Exception {
        ChatMessage errorMessage = ChatMessage.builder()
                                             .type(ChatMessage.Type.ERROR)
                                             .content(error)
                                             .timestamp(Instant.now())
                                             .build();
        sendMessage(session, errorMessage);
    }

    private String getUserId(WebSocketSession session) {
        // 从会话属性中获取用户ID
        Object userId = session.getAttributes().get("userId");
        return userId != null ? userId.toString() : "anonymous_" + session.getId();
    }

    @Override
    public List<String> getSubProtocols() {
        return Arrays.asList("chat", "notification");
    }
}
```

### 2. STOMP 协议支持

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketStompConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理
        config.enableSimpleBroker("/topic", "/queue")
              .setHeartbeatValue(new long[]{10000, 20000}) // 心跳配置
              .setTaskScheduler(taskScheduler()); // 自定义任务调度器

        // 应用程序消息前缀
        config.setApplicationDestinationPrefixes("/app");
        
        // 用户特定消息前缀
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(false)
                .setHeartbeatTime(25000); // SockJS 心跳
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        registration.setMessageSizeLimit(64 * 1024) // 64KB
                   .setSendBufferSizeLimit(512 * 1024) // 512KB
                   .setSendTimeLimit(20000); // 20秒
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new StompChannelInterceptor())
                   .taskExecutor(taskExecutor());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("websocket-");
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("websocket-async-");
        executor.initialize();
        return executor;
    }
}

// STOMP 消息控制器
@Controller
public class StompMessageController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public StompMessageController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    // 处理聊天消息
    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessage sendMessage(@Payload ChatMessage message,
                                  SimpMessageHeaderAccessor headerAccessor) {
        // 从会话中获取用户信息
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSenderId(username);
        message.setTimestamp(Instant.now());
        
        // 保存消息
        ChatMessage savedMessage = chatService.saveMessage(message);
        
        return savedMessage;
    }

    // 处理私信
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload PrivateMessage message,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String sender = (String) headerAccessor.getSessionAttributes().get("username");
        message.setSender(sender);
        message.setTimestamp(Instant.now());
        
        // 发送给特定用户
        messagingTemplate.convertAndSendToUser(
            message.getRecipient(),
            "/queue/private",
            message
        );
        
        // 保存私信记录
        chatService.savePrivateMessage(message);
    }

    // 用户加入聊天室
    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public ChatMessage joinChat(@Payload ChatMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {
        // 在会话中存储用户名
        headerAccessor.getSessionAttributes().put("username", message.getSenderId());
        
        message.setType(ChatMessage.Type.USER_JOINED);
        message.setContent(message.getSenderId() + " 加入了聊天室");
        message.setTimestamp(Instant.now());
        
        return message;
    }

    // 发送通知
    @MessageMapping("/notification.send")
    public void sendNotification(@Payload NotificationMessage notification) {
        // 根据通知类型选择发送目标
        switch (notification.getType()) {
            case BROADCAST:
                messagingTemplate.convertAndSend("/topic/notifications", notification);
                break;
            case USER_SPECIFIC:
                messagingTemplate.convertAndSendToUser(
                    notification.getTargetUser(),
                    "/queue/notifications",
                    notification
                );
                break;
            case ROLE_BASED:
                messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notification.getTargetRole(),
                    notification
                );
                break;
        }
    }
}

// 连接事件监听器
@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("新的WebSocket连接: {}", event.getMessage());
        
        // 获取连接信息
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 可以在这里执行连接后的逻辑
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username != null) {
            logger.info("用户断开连接: {}", username);
            
            // 通知其他用户有人离开
            ChatMessage leaveMessage = ChatMessage.builder()
                                                 .type(ChatMessage.Type.USER_LEFT)
                                                 .senderId(username)
                                                 .content(username + " 离开了聊天室")
                                                 .timestamp(Instant.now())
                                                 .build();
            
            messagingTemplate.convertAndSend("/topic/chat", leaveMessage);
        }
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();
        String sessionId = headerAccessor.getSessionId();
        
        logger.info("会话 {} 订阅了 {}", sessionId, destination);
    }
}
```

### 3. WebSocket 测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private StompSession stompSession;
    private final BlockingQueue<ChatMessage> blockingQueue = new LinkedBlockingDeque<>();

    @BeforeEach
    public void setup() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
            Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))
        ));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:" + port + "/ws-stomp";
        stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command, 
                                      StompHeaders headers, byte[] payload, Throwable exception) {
                exception.printStackTrace();
            }
        }).get(1, TimeUnit.SECONDS);
    }

    @AfterEach
    public void cleanup() {
        if (stompSession != null && stompSession.isConnected()) {
            stompSession.disconnect();
        }
    }

    @Test
    @DisplayName("STOMP 聊天消息测试")
    public void testChatMessage() throws Exception {
        // 订阅聊天频道
        stompSession.subscribe("/topic/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.offer((ChatMessage) payload);
            }
        });

        // 发送消息
        ChatMessage message = ChatMessage.builder()
                                        .content("Hello WebSocket!")
                                        .type(ChatMessage.Type.CHAT)
                                        .build();

        stompSession.send("/app/chat.send", message);

        // 验证接收到的消息
        ChatMessage receivedMessage = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertThat(receivedMessage).isNotNull();
        assertThat(receivedMessage.getContent()).isEqualTo("Hello WebSocket!");
        assertThat(receivedMessage.getType()).isEqualTo(ChatMessage.Type.CHAT);
    }

    @Test
    @DisplayName("多客户端聊天测试")
    public void testMultiClientChat() throws Exception {
        // 创建第二个客户端
        StompSession client2 = createSecondClient();
        BlockingQueue<ChatMessage> client2Queue = new LinkedBlockingDeque<>();

        // 两个客户端都订阅聊天频道
        stompSession.subscribe("/topic/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                blockingQueue.offer((ChatMessage) payload);
            }
        });

        client2.subscribe("/topic/chat", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                client2Queue.offer((ChatMessage) payload);
            }
        });

        // 客户端1发送消息
        ChatMessage message = ChatMessage.builder()
                                        .content("Multi-client test")
                                        .type(ChatMessage.Type.CHAT)
                                        .build();

        stompSession.send("/app/chat.send", message);

        // 验证两个客户端都收到了消息
        ChatMessage received1 = blockingQueue.poll(5, TimeUnit.SECONDS);
        ChatMessage received2 = client2Queue.poll(5, TimeUnit.SECONDS);

        assertThat(received1).isNotNull();
        assertThat(received2).isNotNull();
        assertThat(received1.getContent()).isEqualTo("Multi-client test");
        assertThat(received2.getContent()).isEqualTo("Multi-client test");

        client2.disconnect();
    }

    private StompSession createSecondClient() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
            Arrays.asList(new WebSocketTransport(new StandardWebSocketClient()))
        ));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:" + port + "/ws-stomp";
        return stompClient.connect(url, new StompSessionHandlerAdapter()).get(1, TimeUnit.SECONDS);
    }
}
```

## 🔗 REST 客户端和集成

### 1. RestTemplate 高级使用

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // 自定义HTTP客户端
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        restTemplate.setRequestFactory(factory);
        
        // 添加拦截器
        restTemplate.setInterceptors(Arrays.asList(
            loggingInterceptor(),
            authenticationInterceptor(),
            retryInterceptor()
        ));
        
        // 错误处理器
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            System.out.println("请求: " + request.getMethod() + " " + request.getURI());
            System.out.println("请求头: " + request.getHeaders());
            System.out.println("请求体: " + new String(body, StandardCharsets.UTF_8));
            
            ClientHttpResponse response = execution.execute(request, body);
            
            System.out.println("响应状态: " + response.getStatusCode());
            System.out.println("响应头: " + response.getHeaders());
            
            return response;
        };
    }

    @Bean
    public ClientHttpRequestInterceptor authenticationInterceptor() {
        return (request, body, execution) -> {
            // 添加认证头
            String token = getCurrentAuthToken();
            if (token != null) {
                request.getHeaders().setBearerAuth(token);
            }
            
            return execution.execute(request, body);
        };
    }

    @Bean
    public ClientHttpRequestInterceptor retryInterceptor() {
        return new RetryInterceptor(3, 1000); // 最多重试3次，间隔1秒
    }

    private String getCurrentAuthToken() {
        // 获取当前认证令牌的逻辑
        return "your-auth-token";
    }
}

// 重试拦截器实现
public class RetryInterceptor implements ClientHttpRequestInterceptor {
    
    private final int maxRetries;
    private final long retryDelay;
    
    public RetryInterceptor(int maxRetries, long retryDelay) {
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }
    
    @Override
    public ClientHttpResponse execute(HttpRequest request, byte[] body, 
                                    ClientHttpRequestExecution execution) throws IOException {
        
        IOException lastException = null;
        
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                ClientHttpResponse response = execution.execute(request, body);
                
                // 如果是服务器错误且还有重试次数，则重试
                if (response.getStatusCode().is5xxServerError() && attempt < maxRetries) {
                    Thread.sleep(retryDelay * (attempt + 1)); // 指数退避
                    continue;
                }
                
                return response;
                
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelay * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("重试被中断", ie);
                    }
                }
            }
        }
        
        throw lastException;
    }
}

// 外部服务集成
@Service
public class ExternalServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ExternalServiceClient(RestTemplate restTemplate, 
                               @Value("${external.service.baseUrl}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    // GET 请求
    public UserProfile getUserProfile(String userId) {
        String url = baseUrl + "/users/{userId}/profile";
        
        return restTemplate.getForObject(url, UserProfile.class, userId);
    }

    // POST 请求
    public CreateUserResponse createUser(CreateUserRequest request) {
        String url = baseUrl + "/users";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateUserRequest> entity = new HttpEntity<>(request, headers);
        
        return restTemplate.postForObject(url, entity, CreateUserResponse.class);
    }

    // PUT 请求
    public UpdateUserResponse updateUser(String userId, UpdateUserRequest request) {
        String url = baseUrl + "/users/{userId}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<UpdateUserResponse> response = restTemplate.exchange(
            url, HttpMethod.PUT, entity, UpdateUserResponse.class, userId
        );
        
        return response.getBody();
    }

    // 文件上传
    public UploadResponse uploadFile(String userId, MultipartFile file) throws IOException {
        String url = baseUrl + "/users/{userId}/files";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        
        return restTemplate.postForObject(url, entity, UploadResponse.class, userId);
    }

    // 异步请求
    public CompletableFuture<UserProfile> getUserProfileAsync(String userId) {
        return CompletableFuture.supplyAsync(() -> getUserProfile(userId));
    }

    // 批量请求
    public List<UserProfile> batchGetUserProfiles(List<String> userIds) {
        return userIds.parallelStream()
                     .map(this::getUserProfile)
                     .collect(Collectors.toList());
    }
}
```

### 2. 外部服务测试

```java
@SpringBootTest
public class ExternalServiceIntegrationTest {

    @Autowired
    private ExternalServiceClient externalServiceClient;

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
                                                        .port(8089)
                                                        .build();

    @Test
    @DisplayName("外部服务用户获取测试")
    public void testGetUserProfile() {
        // Given
        String userId = "123";
        UserProfile expectedProfile = UserProfile.builder()
                                                .id(userId)
                                                .name("测试用户")
                                                .email("test@example.com")
                                                .build();

        wireMock.stubFor(get(urlEqualTo("/users/" + userId + "/profile"))
                       .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(expectedProfile))));

        // When
        UserProfile actualProfile = externalServiceClient.getUserProfile(userId);

        // Then
        assertThat(actualProfile).isNotNull();
        assertThat(actualProfile.getId()).isEqualTo(userId);
        assertThat(actualProfile.getName()).isEqualTo("测试用户");

        wireMock.verify(getRequestedFor(urlEqualTo("/users/" + userId + "/profile")));
    }

    @Test
    @DisplayName("外部服务错误处理测试")
    public void testServiceError() {
        // Given
        String userId = "404";
        
        wireMock.stubFor(get(urlEqualTo("/users/" + userId + "/profile"))
                       .willReturn(aResponse()
                                 .withStatus(404)
                                 .withBody("User not found")));

        // When & Then
        assertThatThrownBy(() -> externalServiceClient.getUserProfile(userId))
            .isInstanceOf(HttpClientErrorException.class)
            .hasMessageContaining("404");
    }

    @Test
    @DisplayName("外部服务重试测试")
    public void testServiceRetry() {
        // Given
        String userId = "retry";
        
        wireMock.stubFor(get(urlEqualTo("/users/" + userId + "/profile"))
                       .inScenario("Retry Scenario")
                       .whenScenarioStateIs(STARTED)
                       .willReturn(aResponse()
                                 .withStatus(500)
                                 .withBody("Internal Server Error"))
                       .willSetStateTo("First Retry"));

        wireMock.stubFor(get(urlEqualTo("/users/" + userId + "/profile"))
                       .inScenario("Retry Scenario")
                       .whenScenarioStateIs("First Retry")
                       .willReturn(aResponse()
                                 .withStatus(200)
                                 .withHeader("Content-Type", "application/json")
                                 .withBody(toJson(UserProfile.builder().id(userId).build()))));

        // When
        UserProfile profile = externalServiceClient.getUserProfile(userId);

        // Then
        assertThat(profile).isNotNull();
        assertThat(profile.getId()).isEqualTo(userId);

        // 验证重试了两次
        wireMock.verify(2, getRequestedFor(urlEqualTo("/users/" + userId + "/profile")));
    }

    private String toJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 📊 性能测试和监控

### 1. 性能测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("高并发用户创建性能测试")
    public void testConcurrentUserCreation() throws InterruptedException {
        int numberOfThreads = 50;
        int requestsPerThread = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i;
            new Thread(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        
                        CreateUserRequest request = CreateUserRequest.builder()
                                                                    .name("用户" + threadIndex + "_" + j)
                                                                    .email("user" + threadIndex + "_" + j + "@test.com")
                                                                    .age(25)
                                                                    .build();

                        try {
                            ResponseEntity<UserDTO> response = restTemplate.postForEntity(
                                "/api/users",
                                request,
                                UserDTO.class
                            );

                            if (response.getStatusCode() == HttpStatus.CREATED) {
                                successCount.incrementAndGet();
                            } else {
                                errorCount.incrementAndGet();
                            }
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }

                        long responseTime = System.currentTimeMillis() - startTime;
                        totalResponseTime.addAndGet(responseTime);
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        // 等待所有线程完成，最多等待60秒
        latch.await(60, TimeUnit.SECONDS);

        int totalRequests = numberOfThreads * requestsPerThread;
        double successRate = (double) successCount.get() / totalRequests * 100;
        double averageResponseTime = (double) totalResponseTime.get() / totalRequests;

        System.out.println("性能测试结果:");
        System.out.println("总请求数: " + totalRequests);
        System.out.println("成功请求数: " + successCount.get());
        System.out.println("失败请求数: " + errorCount.get());
        System.out.println("成功率: " + String.format("%.2f%%", successRate));
        System.out.println("平均响应时间: " + String.format("%.2fms", averageResponseTime));

        // 断言性能指标
        assertThat(successRate).isGreaterThan(95.0); // 成功率应大于95%
        assertThat(averageResponseTime).isLessThan(1000.0); // 平均响应时间应小于1秒
    }

    @Test
    @DisplayName("内存使用监控测试")
    public void testMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        
        long beforeMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // 执行大量操作
        for (int i = 0; i < 1000; i++) {
            CreateUserRequest request = CreateUserRequest.builder()
                                                        .name("用户" + i)
                                                        .email("user" + i + "@test.com")
                                                        .age(25)
                                                        .build();
            
            restTemplate.postForEntity("/api/users", request, UserDTO.class);
        }
        
        // 强制垃圾回收
        System.gc();
        
        long afterMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterMemory - beforeMemory;
        
        System.out.println("内存使用情况:");
        System.out.println("执行前内存: " + beforeMemory / 1024 / 1024 + "MB");
        System.out.println("执行后内存: " + afterMemory / 1024 / 1024 + "MB");
        System.out.println("内存增加: " + memoryUsed / 1024 / 1024 + "MB");
        
        // 内存增加应在合理范围内
        assertThat(memoryUsed).isLessThan(100 * 1024 * 1024); // 小于100MB
    }
}
```

### 2. 监控和指标

```java
@Component
public class WebMetricsCollector {

    private final MeterRegistry meterRegistry;
    private final Counter requestCounter;
    private final Timer responseTimer;
    private final Gauge activeUsers;

    public WebMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.requestCounter = Counter.builder("web.requests.total")
                                   .description("Total number of web requests")
                                   .register(meterRegistry);
        this.responseTimer = Timer.builder("web.response.time")
                                 .description("Web response time")
                                 .register(meterRegistry);
        this.activeUsers = Gauge.builder("web.users.active")
                               .description("Number of active users")
                               .register(meterRegistry, this, WebMetricsCollector::getActiveUsersCount);
    }

    public void recordRequest(String method, String uri, int statusCode, long duration) {
        requestCounter.increment(
            Tags.of(
                "method", method,
                "uri", uri,
                "status", String.valueOf(statusCode)
            )
        );

        responseTimer.record(
            duration,
            TimeUnit.MILLISECONDS,
            Tags.of(
                "method", method,
                "uri", uri,
                "status", String.valueOf(statusCode)
            )
        );
    }

    private double getActiveUsersCount() {
        // 获取活跃用户数的实现
        return SessionRegistry.getActiveSessionCount();
    }
}

// 自定义健康检查
@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final ExternalServiceClient externalServiceClient;
    private final UserRepository userRepository;

    public CustomHealthIndicator(ExternalServiceClient externalServiceClient,
                               UserRepository userRepository) {
        this.externalServiceClient = externalServiceClient;
        this.userRepository = userRepository;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        try {
            // 检查数据库连接
            long userCount = userRepository.count();
            builder.withDetail("database.userCount", userCount);

            // 检查外部服务
            long startTime = System.currentTimeMillis();
            try {
                externalServiceClient.getUserProfile("health-check");
                long responseTime = System.currentTimeMillis() - startTime;
                builder.withDetail("externalService.responseTime", responseTime + "ms");
                builder.withDetail("externalService.status", "UP");
            } catch (Exception e) {
                builder.withDetail("externalService.status", "DOWN");
                builder.withDetail("externalService.error", e.getMessage());
            }

            // 检查系统资源
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsage = (double) usedMemory / totalMemory * 100;

            builder.withDetail("system.memory.total", totalMemory / 1024 / 1024 + "MB");
            builder.withDetail("system.memory.used", usedMemory / 1024 / 1024 + "MB");
            builder.withDetail("system.memory.usage", String.format("%.2f%%", memoryUsage));

            if (memoryUsage > 90) {
                builder.down().withDetail("reason", "内存使用率过高");
            } else {
                builder.up();
            }

        } catch (Exception e) {
            builder.down().withException(e);
        }

        return builder.build();
    }
}
```

## 📝 小结

Spring Web 测试与集成技术提供了全面的质量保障体系：

### 测试框架总结

- **分层测试** - 单元测试、集成测试、端到端测试的完整覆盖
- **Mock 支持** - MockMvc、WebTestClient 等强大的模拟测试工具
- **容器化测试** - TestContainers 支持真实环境测试
- **性能测试** - 并发测试和性能监控能力

### WebSocket 技术特点

| 特性 | 优势 | 应用场景 |
|------|------|----------|
| **实时通信** | 低延迟双向通信 | 聊天、通知、实时更新 |
| **STOMP 协议** | 标准化消息传输 | 消息队列、发布订阅 |
| **连接管理** | 自动重连、心跳检测 | 稳定长连接 |
| **集群支持** | 分布式消息代理 | 大规模实时应用 |

### 集成技术优势

1. **服务通信** - RestTemplate、WebClient 等多种客户端支持
2. **错误处理** - 完善的重试和熔断机制
3. **监控指标** - 全面的性能和健康监控
4. **外部集成** - 与第三方服务的无缝集成

### 最佳实践要点

1. **测试策略** - 采用测试金字塔，重点关注单元测试
2. **性能监控** - 建立完整的监控指标体系
3. **错误处理** - 实施完善的错误处理和恢复机制
4. **安全考虑** - WebSocket 连接的认证和授权
5. **资源管理** - 合理管理连接和内存资源

Spring Web 测试与集成技术为构建高质量、高可用的 Web 应用提供了完整的技术支撑，结合现代化的开发和运维工具，能够确保应用在各种环境下的稳定运行。

