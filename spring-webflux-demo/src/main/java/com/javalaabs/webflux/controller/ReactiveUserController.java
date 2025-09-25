package com.javalaabs.webflux.controller;

import com.javalaabs.webflux.domain.dto.CreateUserRequest;
import com.javalaabs.webflux.domain.dto.UpdateUserRequest;
import com.javalaabs.webflux.domain.dto.UserActivityDTO;
import com.javalaabs.webflux.domain.dto.UserDTO;
import com.javalaabs.webflux.domain.event.UserUpdateEvent;
import com.javalaabs.webflux.exception.UserNotFoundException;
import com.javalaabs.webflux.exception.ValidationException;
import com.javalaabs.webflux.service.ReactiveUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应式用户控制器
 * 演示注解式响应式编程模式
 */
@RestController
@RequestMapping("/api/reactive/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReactiveUserController {
    
    private final ReactiveUserService userService;
    
    public ReactiveUserController(ReactiveUserService userService) {
        this.userService = userService;
    }
    
    /**
     * 获取单个用户
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> getUser(@PathVariable String id) {
        return userService.findById(id)
                         .map(ResponseEntity::ok)
                         .onErrorResume(UserNotFoundException.class, 
                             error -> Mono.just(ResponseEntity.notFound().build()))
                         .doOnNext(response -> System.out.println("返回用户: " + id))
                         .doOnError(error -> System.err.println("获取用户失败: " + error.getMessage()));
    }
    
    /**
     * 获取用户列表（支持分页和搜索）
     */
    @GetMapping
    public Flux<UserDTO> getUsers(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String search) {
        
        // 参数验证
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;
        
        return userService.findUsers(page, size, search)
                         .take(size) // 额外保护，确保不超过指定大小
                         .doOnNext(user -> System.out.println("返回用户: " + user.getName()))
                         .doOnComplete(() -> System.out.println("用户列表返回完成"))
                         .onErrorContinue((error, item) -> 
                             System.err.println("处理用户数据出错: " + error.getMessage()));
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    public Mono<ResponseEntity<UserDTO>> createUser(@Valid @RequestBody Mono<CreateUserRequest> userRequestMono) {
        return userRequestMono
            .flatMap(request -> userService.createUser(request))
            .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
            .onErrorResume(ValidationException.class, 
                error -> Mono.just(ResponseEntity.badRequest().build()))
            .onErrorResume(Exception.class,
                error -> {
                    System.err.println("创建用户失败: " + error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }
    
    /**
     * 批量创建用户
     */
    @PostMapping("/batch")
    public Flux<UserDTO> createUsers(@RequestBody Flux<CreateUserRequest> userRequestFlux) {
        return userRequestFlux
            .buffer(10) // 每10个一批处理
            .flatMap(batch -> userService.createUsers(batch))
            .onErrorContinue((error, user) -> 
                System.err.println("批量创建用户失败: " + error.getMessage()))
            .doOnNext(user -> System.out.println("批量创建用户: " + user.getName()))
            .doOnComplete(() -> System.out.println("批量创建完成"));
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserDTO>> updateUser(@PathVariable String id,
                                                   @Valid @RequestBody Mono<UpdateUserRequest> updateRequestMono) {
        return updateRequestMono
            .flatMap(request -> userService.updateUser(id, request))
            .map(updatedUser -> ResponseEntity.ok(updatedUser))
            .onErrorResume(UserNotFoundException.class,
                error -> Mono.just(ResponseEntity.notFound().build()))
            .onErrorResume(ValidationException.class,
                error -> Mono.just(ResponseEntity.badRequest().build()));
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userService.deleteById(id)
                         .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                         .onErrorResume(UserNotFoundException.class,
                             error -> Mono.just(ResponseEntity.notFound().build()));
    }
    
    /**
     * 服务器推送事件 (SSE) - 用户活动流
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UserActivityDTO>> streamUserActivities() {
        return userService.getUserActivityStream()
                         .map(activity -> ServerSentEvent.builder(activity)
                                                        .id(String.valueOf(activity.getId()))
                                                        .event("user-activity")
                                                        .build())
                         .doOnNext(event -> System.out.println("推送用户活动: " + event.data()))
                         .doOnCancel(() -> System.out.println("客户端取消了用户活动流"))
                         .onErrorContinue((error, event) -> 
                             System.err.println("推送活动事件出错: " + error.getMessage()));
    }
    
    /**
     * 流式JSON响应 - 用户更新事件
     */
    @GetMapping(value = "/live-updates", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<UserUpdateEvent> liveUserUpdates(@RequestParam(required = false) String userId) {
        Flux<UserUpdateEvent> stream = userService.getUserUpdateStream();
        
        if (userId != null && !userId.trim().isEmpty()) {
            stream = stream.filter(event -> userId.equals(event.getUserId()));
        }
        
        return stream
            .delayElements(Duration.ofMillis(100)) // 限制推送速率
            .doOnSubscribe(subscription -> System.out.println("客户端订阅用户更新流"))
            .doOnCancel(() -> System.out.println("客户端取消用户更新流"))
            .onErrorContinue((error, event) -> 
                System.err.println("推送更新事件出错: " + error.getMessage()));
    }
    
    /**
     * 文件上传 - 用户头像
     */
    @PostMapping(value = "/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Map<String, String>>> uploadAvatar(@PathVariable String id,
                                                                 @RequestPart("file") Mono<FilePart> filePartMono) {
        return filePartMono
            .flatMap(filePart -> userService.uploadAvatar(id, filePart))
            .map(avatarUrl -> ResponseEntity.ok(Map.of("avatarUrl", avatarUrl)))
            .onErrorResume(UserNotFoundException.class,
                error -> Mono.just(ResponseEntity.notFound().build()))
            .onErrorResume(IllegalArgumentException.class,
                error -> Mono.just(ResponseEntity.badRequest().body(Map.of("error", error.getMessage()))))
            .onErrorResume(Exception.class,
                error -> {
                    System.err.println("上传头像失败: " + error.getMessage());
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                  .body(Map.of("error", "上传失败")));
                });
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> health() {
        return userService.getHealthStatus()
                         .map(status -> {
                             Map<String, Object> healthBody = new HashMap<>();
                             healthBody.put("status", status);
                             healthBody.put("timestamp", Instant.now());
                             healthBody.put("service", "reactive-user-service");
                             return ResponseEntity.ok(healthBody);
                         })
                         .timeout(Duration.ofSeconds(5))
                        .onErrorResume(error -> {
                            Map<String, Object> errorBody = new HashMap<>();
                            errorBody.put("status", "DOWN");
                            errorBody.put("timestamp", Instant.now());
                            ResponseEntity<Map<String, Object>> response = ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(errorBody);
                            return Mono.just(response);
                        });
    }
    
    /**
     * 获取统计信息
     */
    @GetMapping("/statistics")
    public Mono<ResponseEntity<Object>> getStatistics() {
        return userService.getStatistics()
                         .map(stats -> ResponseEntity.ok(stats))
                         .timeout(Duration.ofSeconds(10))
                         .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                     .body(Map.of("error", "获取统计信息失败")));
    }
    
    /**
     * 心跳检测（用于保持连接）
     */
    @GetMapping(value = "/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> heartbeat() {
        return Flux.interval(Duration.ofSeconds(30))
                  .map(tick -> ServerSentEvent.builder("ping")
                                             .id(String.valueOf(tick))
                                             .event("heartbeat")
                                             .build())
                  .doOnSubscribe(subscription -> System.out.println("客户端订阅心跳"))
                  .doOnCancel(() -> System.out.println("客户端取消心跳"));
    }
    
    /**
     * 获取用户详细信息（演示复杂响应式操作）
     */
    @GetMapping("/{id}/details")
    public Mono<ResponseEntity<Map<String, Object>>> getUserDetails(@PathVariable String id) {
        Mono<UserDTO> userMono = userService.findById(id);
        Mono<Object> statsMono = userService.getStatistics();
        
        return Mono.zip(userMono, statsMono)
                  .map(tuple -> {
                      UserDTO user = tuple.getT1();
                      Object stats = tuple.getT2();
                      
                      Map<String, Object> details = Map.of(
                          "user", user,
                          "statistics", stats,
                          "timestamp", Instant.now()
                      );
                      
                      return ResponseEntity.ok(details);
                  })
                  .onErrorResume(UserNotFoundException.class,
                      error -> Mono.just(ResponseEntity.notFound().build()))
                  .timeout(Duration.ofSeconds(15));
    }
    
    /**
     * 批量获取用户（演示并行处理）
     */
    @PostMapping("/batch-get")
    public Flux<UserDTO> batchGetUsers(@RequestBody Flux<String> userIdFlux) {
        return userIdFlux
            .flatMap(userId -> userService.findById(userId), 5) // 并发度为5
            .onErrorContinue(UserNotFoundException.class, (error, userId) -> 
                System.err.println("用户不存在: " + userId))
            .doOnNext(user -> System.out.println("批量获取用户: " + user.getName()));
    }
}
