package com.javalaabs.webflux.service;

import com.javalaabs.webflux.exception.UserNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 外部API服务
 * 演示WebClient响应式客户端的各种使用场景
 */
@Service
public class ExternalApiService {
    
    private final WebClient webClient;
    private final WebClient externalApiClient;
    
    public ExternalApiService(WebClient.Builder webClientBuilder) {
        // 配置默认的WebClient
        this.webClient = webClientBuilder
            .baseUrl("http://localhost:8080")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "WebFlux-Demo-Client/1.0")
            .filter(logRequest())
            .filter(logResponse())
            .filter(retryFilter())
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
            .build();
        
        // 配置外部API客户端
        this.externalApiClient = webClientBuilder
            .baseUrl("https://jsonplaceholder.typicode.com")
            .defaultHeader(HttpHeaders.USER_AGENT, "WebFlux-Demo-External/1.0")
            .filter(logRequest())
            .filter(logResponse())
            .build();
    }
    
    /**
     * 基础GET请求示例
     */
    public Mono<UserInfo> getUserInfo(String userId) {
        return webClient.get()
                       .uri("/api/users/{id}", userId)
                       .retrieve()
                       .onStatus(HttpStatusCode::is4xxClientError, response -> {
                           if (response.statusCode() == HttpStatus.NOT_FOUND) {
                               return Mono.error(new UserNotFoundException("用户不存在: " + userId));
                           }
                           return Mono.error(new RuntimeException("客户端错误: " + response.statusCode()));
                       })
                       .bodyToMono(UserInfo.class)
                       .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                     .filter(throwable -> !(throwable instanceof UserNotFoundException)))
                       .timeout(Duration.ofSeconds(5))
                       .doOnNext(userInfo -> System.out.println("获取用户信息: " + userInfo.getName()));
    }
    
    /**
     * POST请求示例
     */
    public Mono<CreateUserResponse> createUser(CreateUserRequest request) {
        return webClient.post()
                       .uri("/api/users")
                       .contentType(MediaType.APPLICATION_JSON)
                       .bodyValue(request)
                       .retrieve()
                       .bodyToMono(CreateUserResponse.class)
                       .onErrorMap(WebClientResponseException.class, ex -> {
                           if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                               return new RuntimeException("邮箱已存在");
                           }
                           return new RuntimeException("创建用户失败: " + ex.getMessage());
                       });
    }
    
    /**
     * 流式响应处理
     */
    public Flux<UserEvent> streamUserEvents() {
        return webClient.get()
                       .uri("/api/users/stream")
                       .accept(MediaType.TEXT_EVENT_STREAM)
                       .retrieve()
                       .bodyToFlux(UserEvent.class)
                       .doOnNext(event -> System.out.println("接收到用户事件: " + event.getType()))
                       .doOnError(error -> System.err.println("流连接错误: " + error.getMessage()))
                       .retry(3);
    }
    
    /**
     * 批量请求示例 - 并行处理
     */
    public Flux<UserInfo> batchGetUsers(List<String> userIds) {
        return Flux.fromIterable(userIds)
                  .flatMap(userId -> getUserInfo(userId), 5) // 并发度为5
                  .onErrorContinue((error, userId) -> 
                      System.err.println("获取用户 " + userId + " 失败: " + error.getMessage()));
    }
    
    /**
     * 并行请求聚合示例
     */
    public Mono<UserProfile> getUserProfile(String userId) {
        Mono<UserInfo> userInfo = getUserInfo(userId);
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
    
    /**
     * 链式请求示例
     */
    public Mono<OrderDetails> getOrderWithUserInfo(String orderId) {
        return getOrder(orderId)
            .flatMap(order -> 
                getUserInfo(order.getUserId())
                    .map(userInfo -> OrderDetails.builder()
                                                .order(order)
                                                .userInfo(userInfo)
                                                .build())
            );
    }
    
    /**
     * 条件请求示例
     */
    public Mono<ProcessResult> processUserAction(String userId, String action) {
        return getUserInfo(userId)
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
    
    /**
     * 外部API调用示例
     */
    public Flux<ExternalPost> getExternalPosts() {
        return externalApiClient.get()
                               .uri("/posts")
                               .retrieve()
                               .bodyToFlux(ExternalPost.class)
                               .take(10) // 只取前10条
                               .timeout(Duration.ofSeconds(10))
                               .onErrorResume(error -> {
                                   System.err.println("获取外部API数据失败: " + error.getMessage());
                                   return Flux.empty();
                               });
    }
    
    /**
     * 文件上传示例
     */
    public Mono<UploadResponse> uploadFile(String userId, byte[] fileData, String fileName) {
        return webClient.post()
                       .uri("/api/users/{id}/avatar", userId)
                       .contentType(MediaType.MULTIPART_FORM_DATA)
                       .bodyValue(createMultipartData(fileData, fileName))
                       .retrieve()
                       .bodyToMono(UploadResponse.class);
    }
    
    /**
     * 条件请求（ETag支持）
     */
    public Mono<UserInfo> getUserInfoIfModified(String userId, String etag) {
        return webClient.get()
                       .uri("/api/users/{id}", userId)
                       .header(HttpHeaders.IF_NONE_MATCH, etag)
                       .retrieve()
                       .onStatus(HttpStatus.NOT_MODIFIED::equals, response -> Mono.empty())
                       .bodyToMono(UserInfo.class);
    }
    
    /**
     * 复杂查询示例
     */
    public Flux<UserInfo> searchUsers(String query, int page, int size) {
        return webClient.get()
                       .uri(uriBuilder -> uriBuilder
                           .path("/api/users")
                           .queryParam("search", query)
                           .queryParam("page", page)
                           .queryParam("size", size)
                           .build())
                       .retrieve()
                       .bodyToFlux(UserInfo.class);
    }
    
    // 私有辅助方法
    private Mono<List<Order>> getUserOrders(String userId) {
        return webClient.get()
                       .uri("/api/users/{id}/orders", userId)
                       .retrieve()
                       .bodyToFlux(Order.class)
                       .collectList()
                       .timeout(Duration.ofSeconds(5))
                       .onErrorReturn(List.of()); // 出错时返回空列表
    }
    
    private Mono<UserPreferences> getUserPreferences(String userId) {
        return webClient.get()
                       .uri("/api/users/{id}/preferences", userId)
                       .retrieve()
                       .bodyToMono(UserPreferences.class)
                       .timeout(Duration.ofSeconds(3))
                       .onErrorReturn(new UserPreferences()); // 出错时返回默认偏好
    }
    
    private Mono<Order> getOrder(String orderId) {
        return webClient.get()
                       .uri("/api/orders/{id}", orderId)
                       .retrieve()
                       .bodyToMono(Order.class);
    }
    
    private Mono<ProcessResult> processPremiumAction(String userId, String action) {
        return webClient.post()
                       .uri("/api/premium/actions")
                       .bodyValue(Map.of("userId", userId, "action", action))
                       .retrieve()
                       .bodyToMono(ProcessResult.class);
    }
    
    private Mono<ProcessResult> processStandardAction(String userId, String action) {
        return webClient.post()
                       .uri("/api/standard/actions")
                       .bodyValue(Map.of("userId", userId, "action", action))
                       .retrieve()
                       .bodyToMono(ProcessResult.class);
    }
    
    private Object createMultipartData(byte[] fileData, String fileName) {
        // 简化实现，实际项目中应该使用 MultipartBodyBuilder
        return Map.of("file", fileData, "filename", fileName);
    }
    
    // 过滤器定义
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("WebClient 请求: " + clientRequest.method() + " " + clientRequest.url());
            clientRequest.headers().forEach((name, values) -> 
                values.forEach(value -> System.out.println("Header: " + name + ": " + value)));
            return Mono.just(clientRequest);
        });
    }
    
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            System.out.println("WebClient 响应状态: " + clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
    
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is5xxServerError()) {
                return Mono.error(new RuntimeException("服务器错误，状态码: " + clientResponse.statusCode()));
            }
            return Mono.just(clientResponse);
        });
    }
    
    // 内部DTO类
    public static class UserInfo {
        private String id;
        private String name;
        private String email;
        private String accountType;
        
        // 构造函数、getter、setter
        public UserInfo() {}
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getAccountType() { return accountType; }
        public void setAccountType(String accountType) { this.accountType = accountType; }
    }
    
    public static class CreateUserRequest {
        private String name;
        private String email;
        
        public CreateUserRequest() {}
        public CreateUserRequest(String name, String email) {
            this.name = name;
            this.email = email;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class CreateUserResponse {
        private String id;
        private String name;
        private String email;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }
    
    public static class UserEvent {
        private String type;
        private String userId;
        private Object data;
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
    
    public static class Order {
        private String id;
        private String userId;
        
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    public static class UserPreferences {
        private String theme = "default";
        private String language = "zh-CN";
        
        public String getTheme() { return theme; }
        public void setTheme(String theme) { this.theme = theme; }
        
        public String getLanguage() { return language; }
        public void setLanguage(String language) { this.language = language; }
    }
    
    public static class UserProfile {
        private UserInfo userInfo;
        private List<Order> orders;
        private UserPreferences preferences;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private UserProfile profile = new UserProfile();
            
            public Builder userInfo(UserInfo userInfo) {
                profile.userInfo = userInfo;
                return this;
            }
            
            public Builder orders(List<Order> orders) {
                profile.orders = orders;
                return this;
            }
            
            public Builder preferences(UserPreferences preferences) {
                profile.preferences = preferences;
                return this;
            }
            
            public UserProfile build() { return profile; }
        }
        
        public UserInfo getUserInfo() { return userInfo; }
        public List<Order> getOrders() { return orders; }
        public UserPreferences getPreferences() { return preferences; }
    }
    
    public static class OrderDetails {
        private Order order;
        private UserInfo userInfo;
        
        public static Builder builder() { return new Builder(); }
        
        public static class Builder {
            private OrderDetails details = new OrderDetails();
            
            public Builder order(Order order) {
                details.order = order;
                return this;
            }
            
            public Builder userInfo(UserInfo userInfo) {
                details.userInfo = userInfo;
                return this;
            }
            
            public OrderDetails build() { return details; }
        }
        
        public Order getOrder() { return order; }
        public UserInfo getUserInfo() { return userInfo; }
    }
    
    public static class ProcessResult {
        private boolean success;
        private String message;
        
        public static ProcessResult failed(String message) {
            ProcessResult result = new ProcessResult();
            result.success = false;
            result.message = message;
            return result;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
    
    public static class ExternalPost {
        private int id;
        private String title;
        private String body;
        
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
    }
    
    public static class UploadResponse {
        private String url;
        private String message;
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
