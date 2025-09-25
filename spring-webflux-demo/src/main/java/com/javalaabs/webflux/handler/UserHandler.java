package com.javalaabs.webflux.handler;

import com.javalaabs.webflux.domain.dto.CreateUserRequest;
import com.javalaabs.webflux.domain.dto.UpdateUserRequest;
import com.javalaabs.webflux.domain.dto.UserDTO;
import com.javalaabs.webflux.domain.event.UserUpdateEvent;
import com.javalaabs.webflux.exception.UserNotFoundException;
import com.javalaabs.webflux.exception.ValidationException;
import com.javalaabs.webflux.service.ReactiveUserService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务处理器
 * 演示函数式编程风格的响应式处理器
 */
@Component
public class UserHandler {
    
    private final ReactiveUserService userService;
    private final Validator validator;
    
    public UserHandler(ReactiveUserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }
    
    /**
     * 获取所有用户
     */
    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        int page = request.queryParam("page")
                         .map(Integer::parseInt)
                         .orElse(0);
        int size = request.queryParam("size")
                         .map(Integer::parseInt)
                         .orElse(10);
        String search = request.queryParam("search").orElse(null);
        
        // 参数验证
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;
        
        Flux<UserDTO> users = userService.findUsers(page, size, search);
        
        return ServerResponse.ok()
                           .contentType(MediaType.APPLICATION_JSON)
                           .body(users, UserDTO.class)
                           .doOnNext(response -> System.out.println("函数式处理器返回用户列表"));
    }
    
    /**
     * 获取单个用户
     */
    public Mono<ServerResponse> getUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return userService.findById(id)
                         .flatMap(user -> ServerResponse.ok()
                                                      .contentType(MediaType.APPLICATION_JSON)
                                                      .bodyValue(user))
                         .switchIfEmpty(ServerResponse.notFound().build())
                         .onErrorResume(UserNotFoundException.class,
                             error -> ServerResponse.notFound().build())
                         .doOnNext(response -> System.out.println("函数式处理器返回用户: " + id));
    }
    
    /**
     * 创建用户
     */
    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                     .doOnNext(this::validateCreateRequest)
                     .flatMap(userService::createUser)
                     .flatMap(user -> ServerResponse.status(HttpStatus.CREATED)
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .bodyValue(user))
                     .onErrorResume(ValidationException.class, 
                         error -> ServerResponse.badRequest()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .bodyValue(Map.of("error", error.getMessage())))
                     .onErrorResume(Exception.class,
                         error -> {
                             System.err.println("函数式创建用户失败: " + error.getMessage());
                             return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .bodyValue(Map.of("error", "内部服务器错误"));
                         });
    }
    
    /**
     * 更新用户
     */
    public Mono<ServerResponse> updateUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return request.bodyToMono(UpdateUserRequest.class)
                     .doOnNext(this::validateUpdateRequest)
                     .flatMap(updateRequest -> userService.updateUser(id, updateRequest))
                     .flatMap(user -> ServerResponse.ok()
                                                  .contentType(MediaType.APPLICATION_JSON)
                                                  .bodyValue(user))
                     .onErrorResume(UserNotFoundException.class,
                         error -> ServerResponse.notFound().build())
                     .onErrorResume(ValidationException.class,
                         error -> ServerResponse.badRequest()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .bodyValue(Map.of("error", error.getMessage())));
    }
    
    /**
     * 删除用户
     */
    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        String id = request.pathVariable("id");
        
        return userService.deleteById(id)
                         .then(ServerResponse.noContent().build())
                         .onErrorResume(UserNotFoundException.class,
                             error -> ServerResponse.notFound().build());
    }
    
    /**
     * 流式响应 - 用户活动
     */
    public Mono<ServerResponse> streamUserActivities(ServerRequest request) {
        var activityStream = userService.getUserActivityStream()
                                       .map(activity -> ServerSentEvent.builder(activity)
                                                                      .id(String.valueOf(activity.getId()))
                                                                      .event("user-activity")
                                                                      .build());
        
        return ServerResponse.ok()
                           .contentType(MediaType.TEXT_EVENT_STREAM)
                           .body(activityStream, ServerSentEvent.class);
    }
    
    /**
     * 文件上传处理
     */
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
                     .flatMap(avatarUrl -> ServerResponse.ok()
                                                       .contentType(MediaType.APPLICATION_JSON)
                                                       .bodyValue(Map.of("avatarUrl", avatarUrl)))
                     .onErrorResume(UserNotFoundException.class,
                         error -> ServerResponse.notFound().build())
                     .onErrorResume(IllegalArgumentException.class,
                         error -> ServerResponse.badRequest()
                                               .contentType(MediaType.APPLICATION_JSON)
                                               .bodyValue(Map.of("error", error.getMessage())));
    }
    
    /**
     * 服务器推送事件端点
     */
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
                // 添加心跳事件
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
    
    /**
     * 批量操作处理
     */
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
                         
                         return ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body(results, UserDTO.class);
                     });
    }
    
    /**
     * 获取用户统计信息
     */
    public Mono<ServerResponse> getStatistics(ServerRequest request) {
        return userService.getStatistics()
                         .flatMap(stats -> ServerResponse.ok()
                                                       .contentType(MediaType.APPLICATION_JSON)
                                                       .bodyValue(stats))
                         .timeout(Duration.ofSeconds(10))
                         .onErrorResume(error -> 
                             ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                         .contentType(MediaType.APPLICATION_JSON)
                                         .bodyValue(Map.of("error", "获取统计信息失败")));
    }
    
    /**
     * 搜索用户（复杂查询示例）
     */
    public Mono<ServerResponse> searchUsers(ServerRequest request) {
        String query = request.queryParam("q").orElse("");
        int page = request.queryParam("page")
                         .map(Integer::parseInt)
                         .orElse(0);
        int size = request.queryParam("size")
                         .map(Integer::parseInt)
                         .orElse(10);
        
        if (query.trim().isEmpty()) {
            return ServerResponse.badRequest()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Map.of("error", "搜索关键字不能为空"));
        }
        
        Flux<UserDTO> searchResults = userService.findUsers(page, size, query)
                                                .take(size);
        
        return ServerResponse.ok()
                           .contentType(MediaType.APPLICATION_JSON)
                           .body(searchResults, UserDTO.class);
    }
    
    /**
     * 流式JSON响应
     */
    public Mono<ServerResponse> streamJsonUsers(ServerRequest request) {
        Flux<UserDTO> userStream = userService.findUsers(0, 1000, null)
                                             .delayElements(Duration.ofMillis(50)); // 控制流速
        
        return ServerResponse.ok()
                           .contentType(MediaType.APPLICATION_NDJSON)
                           .body(userStream, UserDTO.class);
    }
    
    /**
     * 健康检查
     */
    public Mono<ServerResponse> health(ServerRequest request) {
        return userService.getHealthStatus()
                         .flatMap(status -> {
                             Map<String, Object> healthInfo = Map.of(
                                 "status", status,
                                 "service", "user-service",
                                 "timestamp", System.currentTimeMillis()
                             );
                             
                             HttpStatus httpStatus = "UP".equals(status) ? 
                                 HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
                             
                             return ServerResponse.status(httpStatus)
                                                 .contentType(MediaType.APPLICATION_JSON)
                                                 .bodyValue(healthInfo);
                         })
                         .timeout(Duration.ofSeconds(5))
                        .onErrorResume(error -> 
                            ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .bodyValue(Map.of("status", "DOWN"))
                        );
    }
    
    // 私有验证方法
    private void validateCreateRequest(CreateUserRequest request) {
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
    
    /**
     * 批量请求DTO
     */
    public static class BatchUserRequest {
        private String operation;
        private String userId;
        private CreateUserRequest createRequest;
        private UpdateUserRequest updateRequest;
        
        // Getters and setters
        public String getOperation() {
            return operation;
        }
        
        public void setOperation(String operation) {
            this.operation = operation;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public CreateUserRequest getCreateRequest() {
            return createRequest;
        }
        
        public void setCreateRequest(CreateUserRequest createRequest) {
            this.createRequest = createRequest;
        }
        
        public UpdateUserRequest getUpdateRequest() {
            return updateRequest;
        }
        
        public void setUpdateRequest(UpdateUserRequest updateRequest) {
            this.updateRequest = updateRequest;
        }
    }
}
