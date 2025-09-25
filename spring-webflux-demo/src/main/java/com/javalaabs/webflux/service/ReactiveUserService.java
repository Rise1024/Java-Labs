package com.javalaabs.webflux.service;

import com.javalaabs.webflux.domain.dto.CreateUserRequest;
import com.javalaabs.webflux.domain.dto.UpdateUserRequest;
import com.javalaabs.webflux.domain.dto.UserActivityDTO;
import com.javalaabs.webflux.domain.dto.UserDTO;
import com.javalaabs.webflux.domain.entity.User;
import com.javalaabs.webflux.domain.entity.UserActivity;
import com.javalaabs.webflux.domain.event.UserCreatedEvent;
import com.javalaabs.webflux.domain.event.UserUpdateEvent;
import com.javalaabs.webflux.exception.DuplicateEmailException;
import com.javalaabs.webflux.exception.UserNotFoundException;
import com.javalaabs.webflux.repository.ReactiveUserActivityRepository;
import com.javalaabs.webflux.repository.ReactiveUserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 响应式用户服务实现类
 * 演示各种响应式编程模式和最佳实践
 */
@Service
public class ReactiveUserService {
    
    private final ReactiveUserRepository userRepository;
    private final ReactiveUserActivityRepository activityRepository;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ApplicationEventPublisher eventPublisher;
    
    // 用于实时事件流的Sink
    private final Sinks.Many<UserUpdateEvent> userUpdateSink;
    private final Sinks.Many<UserActivityDTO> activitySink;
    
    public ReactiveUserService(ReactiveUserRepository userRepository,
                             ReactiveUserActivityRepository activityRepository,
                             ApplicationEventPublisher eventPublisher,
                             @Autowired(required = false) ReactiveRedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.redisTemplate = redisTemplate;
        this.eventPublisher = eventPublisher;
        
        // 初始化实时事件流
        this.userUpdateSink = Sinks.many().multicast().onBackpressureBuffer();
        this.activitySink = Sinks.many().multicast().onBackpressureBuffer();
    }
    
    /**
     * 根据ID查找用户（支持缓存）
     */
    public Mono<UserDTO> findById(String id) {
        String cacheKey = "user:" + id;
        
        // 如果Redis可用，使用缓存；否则直接查询数据库
        if (redisTemplate != null) {
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
                                               .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                               )
                               .doOnNext(user -> logActivity(user.getId(), "VIEW_PROFILE", "查看用户资料"));
        } else {
            return userRepository.findById(id)
                               .map(this::convertToDTO)
                               .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                               .doOnNext(user -> logActivity(user.getId(), "VIEW_PROFILE", "查看用户资料"));
        }
    }
    
    /**
     * 分页查询用户
     */
    public Flux<UserDTO> findUsers(int page, int size, String search) {
        long offset = (long) page * size;
        
        Flux<User> userFlux;
        if (search != null && !search.trim().isEmpty()) {
            userFlux = userRepository.searchUsersWithPaging(search.trim(), size, offset);
        } else {
            userFlux = userRepository.findUsersWithPaging(size, offset);
        }
        
        return userFlux
            .map(this::convertToDTO)
            .doOnNext(user -> System.out.println("查询到用户: " + user.getName()))
            .onErrorContinue((error, user) -> 
                System.err.println("处理用户数据出错: " + error.getMessage()));
    }
    
    /**
     * 创建用户（带事务）
     */
    @Transactional
    public Mono<UserDTO> createUser(CreateUserRequest request) {
        // 数据预处理
        request.normalizeEmail();
        request.normalizeName();
        
        return userRepository.existsByEmail(request.getEmail())
                           .flatMap(exists -> {
                               if (exists) {
                                   return Mono.error(new DuplicateEmailException(request.getEmail()));
                               }
                               
                               User user = convertToEntity(request);
                               user.setId(UUID.randomUUID().toString());
                               
                               return userRepository.save(user);
                           })
                           .map(this::convertToDTO)
                           .flatMap(userDTO -> {
                               // 发布用户创建事件
                               eventPublisher.publishEvent(new UserCreatedEvent(userDTO.getId(), userDTO.getEmail()));
                               
                               // 记录活动
                               return logActivity(userDTO.getId(), "CREATE_USER", "用户注册")
                                   .thenReturn(userDTO);
                           })
                           .flatMap(userDTO -> {
                               // 发送实时更新事件
                               UserUpdateEvent updateEvent = UserUpdateEvent.builder()
                                   .userId(userDTO.getId())
                                   .eventType(UserUpdateEvent.EventTypes.USER_CREATED)
                                   .build();
                               
                               userUpdateSink.tryEmitNext(updateEvent);
                               return Mono.just(userDTO);
                           })
                           .doOnNext(user -> System.out.println("创建用户成功: " + user.getName()));
    }
    
    /**
     * 批量创建用户
     */
    @Transactional
    public Flux<UserDTO> createUsers(List<CreateUserRequest> requests) {
        return Flux.fromIterable(requests)
                  .map(request -> {
                      request.normalizeEmail();
                      request.normalizeName();
                      return request;
                  })
                  .flatMap(request -> 
                      userRepository.existsByEmail(request.getEmail())
                                   .flatMap(exists -> {
                                       if (exists) {
                                           return Mono.empty(); // 跳过已存在的邮箱
                                       }
                                       
                                       User user = convertToEntity(request);
                                       user.setId(UUID.randomUUID().toString());
                                       return userRepository.save(user);
                                   })
                  )
                  .map(this::convertToDTO)
                  .flatMap(userDTO -> {
                      eventPublisher.publishEvent(new UserCreatedEvent(userDTO.getId(), userDTO.getEmail()));
                      
                      return logActivity(userDTO.getId(), "CREATE_USER", "批量创建用户")
                          .thenReturn(userDTO);
                  })
                  .onErrorContinue((error, user) -> 
                      System.err.println("批量创建用户失败: " + error.getMessage()));
    }
    
    /**
     * 更新用户信息
     */
    @Transactional
    public Mono<UserDTO> updateUser(String id, UpdateUserRequest request) {
        return userRepository.findById(id)
                           .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                           .flatMap(existingUser -> {
                               // 应用更新
                               if (request.hasName()) {
                                   existingUser.setName(request.getName().trim());
                               }
                               if (request.hasEmail()) {
                                   existingUser.setEmail(request.getEmail().trim().toLowerCase());
                               }
                               if (request.hasAvatarUrl()) {
                                   existingUser.setAvatarUrl(request.getAvatarUrl());
                               }
                               if (request.hasAccountType()) {
                                   existingUser.setAccountType(request.getAccountType());
                               }
                               if (request.hasActiveStatus()) {
                                   existingUser.setIsActive(request.getIsActive());
                               }
                               
                               existingUser.updateLastModified();
                               
                               return userRepository.save(existingUser);
                           })
                           .map(this::convertToDTO)
                           .flatMap(userDTO -> {
                               // 清除缓存（如果Redis可用）
                               String cacheKey = "user:" + userDTO.getId();
                               Mono<Void> cacheOp = redisTemplate != null ? 
                                   redisTemplate.delete(cacheKey).then() : 
                                   Mono.empty();
                               return cacheOp
                                   .then(logActivity(userDTO.getId(), "UPDATE_USER", "更新用户信息"))
                                   .thenReturn(userDTO);
                           })
                           .flatMap(userDTO -> {
                               // 发送实时更新事件
                               UserUpdateEvent updateEvent = UserUpdateEvent.builder()
                                   .userId(userDTO.getId())
                                   .eventType(UserUpdateEvent.EventTypes.USER_UPDATED)
                                   .build();
                               
                               userUpdateSink.tryEmitNext(updateEvent);
                               return Mono.just(userDTO);
                           })
                           .doOnNext(user -> System.out.println("更新用户成功: " + user.getName()));
    }
    
    /**
     * 删除用户
     */
    @Transactional
    public Mono<Void> deleteById(String id) {
        return userRepository.findById(id)
                           .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
                           .flatMap(user -> {
                               // 先记录删除活动
                               return logActivity(user.getId(), "DELETE_USER", "删除用户")
                                   .then(activityRepository.deleteByUserId(user.getId())) // 删除活动记录
                                   .then(userRepository.delete(user)); // 删除用户
                           })
                           .then(Mono.fromRunnable(() -> {
                               // 清除缓存（如果Redis可用）
                               if (redisTemplate != null) {
                                   String cacheKey = "user:" + id;
                                   redisTemplate.delete(cacheKey).subscribe();
                               }
                               
                               // 发送实时更新事件
                               UserUpdateEvent updateEvent = UserUpdateEvent.builder()
                                   .userId(id)
                                   .eventType(UserUpdateEvent.EventTypes.USER_DELETED)
                                   .build();
                               
                               userUpdateSink.tryEmitNext(updateEvent);
                           }))
                           .doOnSuccess(v -> System.out.println("删除用户成功: " + id))
                           .then();
    }
    
    /**
     * 文件上传处理
     */
    public Mono<String> uploadAvatar(String userId, FilePart filePart) {
        return validateImageFile(filePart)
            .then(saveFile(filePart))
            .flatMap(fileName -> updateUserAvatar(userId, fileName))
            .flatMap(avatarUrl -> 
                logActivity(userId, "UPLOAD_AVATAR", "上传头像: " + avatarUrl)
                    .thenReturn(avatarUrl)
            )
            .doOnNext(avatarUrl -> System.out.println("用户 " + userId + " 上传头像: " + avatarUrl))
            .subscribeOn(Schedulers.boundedElastic()); // 文件操作使用有界弹性调度器
    }
    
    /**
     * 获取用户活动流（实时）
     */
    public Flux<UserActivityDTO> getUserActivityStream() {
        return activitySink.asFlux()
                          .onBackpressureBuffer(1000)
                          .share(); // 共享流，避免多个订阅者重复执行
    }
    
    /**
     * 获取用户更新流（实时）
     */
    public Flux<UserUpdateEvent> getUserUpdateStream() {
        return userUpdateSink.asFlux()
                            .onBackpressureBuffer(1000)
                            .share();
    }
    
    /**
     * 健康检查
     */
    public Mono<String> getHealthStatus() {
        return userRepository.count()
                           .map(count -> count >= 0 ? "UP" : "DOWN")
                           .timeout(Duration.ofSeconds(5))
                           .onErrorReturn("DOWN");
    }
    
    /**
     * 获取统计信息
     */
    public Mono<Object> getStatistics() {
        return Mono.zip(
            userRepository.countUsers(),
            userRepository.countActiveUsers(),
            userRepository.countByAccountType("premium")
        ).map(tuple -> {
            return new Object() {
                public final long totalUsers = tuple.getT1();
                public final long activeUsers = tuple.getT2();
                public final long premiumUsers = tuple.getT3();
            };
        });
    }
    
    // 私有辅助方法
    private Mono<Void> validateImageFile(FilePart filePart) {
        return Mono.fromRunnable(() -> {
            String filename = filePart.filename();
            if (filename == null || (!filename.endsWith(".jpg") && !filename.endsWith(".png") && !filename.endsWith(".jpeg"))) {
                throw new IllegalArgumentException("只支持 JPG、JPEG 和 PNG 格式的图片");
            }
        });
    }
    
    private Mono<String> saveFile(FilePart filePart) {
        String fileName = UUID.randomUUID() + "_" + filePart.filename();
        // 实际项目中这里应该保存到文件系统或云存储
        // 这里仅返回文件名作为示例
        return Mono.just(fileName)
                  .delayElement(Duration.ofMillis(100)); // 模拟文件保存时间
    }
    
    private Mono<String> updateUserAvatar(String userId, String fileName) {
        String avatarUrl = "/api/files/" + fileName;
        
        return userRepository.findById(userId)
                           .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                           .flatMap(user -> {
                               user.setAvatarUrl(avatarUrl);
                               user.updateLastModified();
                               return userRepository.save(user);
                           })
                           .then(Mono.fromRunnable(() -> {
                               // 清除缓存（如果Redis可用）
                               if (redisTemplate != null) {
                                   String cacheKey = "user:" + userId;
                                   redisTemplate.delete(cacheKey).subscribe();
                               }
                           }))
                           .thenReturn(avatarUrl);
    }
    
    private Mono<Void> logActivity(String userId, String action, String description) {
        UserActivity activity = UserActivity.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .action(action)
            .description(description)
            .timestamp(Instant.now())
            .build();
        
        return activityRepository.save(activity)
                                .map(this::convertToActivityDTO)
                                .doOnNext(activityDTO -> activitySink.tryEmitNext(activityDTO))
                                .then();
    }
    
    // 转换方法
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                     .id(user.getId())
                     .name(user.getName())
                     .email(user.getEmail())
                     .avatarUrl(user.getAvatarUrl())
                     .accountType(user.getAccountType())
                     .createTime(user.getCreateTime())
                     .updateTime(user.getUpdateTime())
                     .isActive(user.getIsActive())
                     .build();
    }
    
    private User convertToEntity(CreateUserRequest request) {
        return User.builder()
                  .name(request.getName())
                  .email(request.getEmail())
                  .accountType(request.getAccountType() != null ? request.getAccountType() : "standard")
                  .createTime(Instant.now())
                  .updateTime(Instant.now())
                  .isActive(true)
                  .build();
    }
    
    private UserActivityDTO convertToActivityDTO(UserActivity activity) {
        return UserActivityDTO.builder()
                             .id(activity.getId())
                             .userId(activity.getUserId())
                             .action(activity.getAction())
                             .description(activity.getDescription())
                             .ipAddress(activity.getIpAddress())
                             .userAgent(activity.getUserAgent())
                             .timestamp(activity.getTimestamp())
                             .build();
    }
}
