package com.javalaabs.webflux.config;

import com.javalaabs.webflux.handler.HealthHandler;
import com.javalaabs.webflux.handler.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

/**
 * 函数式路由配置
 * 配置所有的函数式端点路由
 */
@Configuration
public class RouterConfiguration {
    
    /**
     * 用户相关路由
     */
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
                    .andRoute(GET("/search").and(queryParam("q", t -> true)), userHandler::searchUsers)
                    .andRoute(GET("/statistics"), userHandler::getStatistics)
                    .andRoute(GET("/stream").and(accept(MediaType.TEXT_EVENT_STREAM)), userHandler::streamUserActivities)
                    .andRoute(GET("/stream-json").and(accept(MediaType.APPLICATION_NDJSON)), userHandler::streamJsonUsers)
                    .andRoute(GET("/sse"), userHandler::sseEndpoint)
                    .andRoute(POST("/batch"), userHandler::batchOperation)
                    .andRoute(POST("/{userId}/upload"), userHandler::uploadFile)
            );
    }
    
    /**
     * 健康检查路由
     */
    @Bean
    public RouterFunction<ServerResponse> healthRoutes(HealthHandler healthHandler) {
        return RouterFunctions
            .nest(path("/actuator"),
                RouterFunctions
                    .route(GET("/health"), healthHandler::health)
                    .andRoute(GET("/health/detailed"), healthHandler::detailedHealth)
                    .andRoute(GET("/health/readiness"), healthHandler::readiness)
                    .andRoute(GET("/health/liveness"), healthHandler::liveness)
                    .andRoute(GET("/metrics"), healthHandler::metrics)
            );
    }
    
    /**
     * 静态资源路由（如果需要）
     */
    @Bean
    public RouterFunction<ServerResponse> staticRoutes() {
        return RouterFunctions
            .resources("/static/**", new org.springframework.core.io.ClassPathResource("static/"))
            .andRoute(GET("/"), 
                request -> ServerResponse.ok()
                                       .contentType(MediaType.TEXT_HTML)
                                       .bodyValue(getWelcomeHtml()));
    }
    
    /**
     * API 文档路由
     */
    @Bean
    public RouterFunction<ServerResponse> apiDocRoutes() {
        return RouterFunctions
            .route(GET("/api-docs"), 
                request -> ServerResponse.ok()
                                       .contentType(MediaType.APPLICATION_JSON)
                                       .bodyValue(getApiDocumentation()))
            .andRoute(GET("/api"), 
                request -> ServerResponse.ok()
                                       .contentType(MediaType.TEXT_HTML)
                                       .bodyValue(getApiDocumentationHtml()));
    }
    
    private String getWelcomeHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Spring WebFlux Demo</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .container { max-width: 800px; margin: 0 auto; }
                    .endpoint { background: #f5f5f5; padding: 10px; margin: 10px 0; border-left: 4px solid #007acc; }
                    .method { font-weight: bold; color: #007acc; }
                    h1 { color: #333; }
                    h2 { color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🚀 Spring WebFlux 响应式编程演示</h1>
                    <p>欢迎使用 Spring WebFlux 学习项目！这个项目演示了响应式编程的各种特性。</p>
                    
                    <h2>📚 可用的 API 端点</h2>
                    
                    <h3>注解式控制器 (Annotation-based)</h3>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/reactive/users - 获取用户列表
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/reactive/users/{id} - 获取单个用户
                    </div>
                    <div class="endpoint">
                        <span class="method">POST</span> /api/reactive/users - 创建用户
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/reactive/users/stream - SSE 用户活动流
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/reactive/users/live-updates - NDJSON 用户更新流
                    </div>
                    
                    <h3>函数式端点 (Functional)</h3>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/users - 获取用户列表
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/users/{id} - 获取单个用户
                    </div>
                    <div class="endpoint">
                        <span class="method">POST</span> /api/users - 创建用户
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /api/users/sse - SSE 事件流
                    </div>
                    
                    <h3>健康检查</h3>
                    <div class="endpoint">
                        <span class="method">GET</span> /actuator/health - 基础健康检查
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /actuator/health/detailed - 详细健康检查
                    </div>
                    <div class="endpoint">
                        <span class="method">GET</span> /actuator/metrics - 应用指标
                    </div>
                    
                    <h2>🎯 特性演示</h2>
                    <ul>
                        <li><strong>响应式编程</strong> - 基于 Reactor 的 Mono/Flux</li>
                        <li><strong>非阻塞I/O</strong> - 高并发处理能力</li>
                        <li><strong>服务器推送事件</strong> - 实时数据流</li>
                        <li><strong>函数式编程</strong> - 函数式路由处理</li>
                        <li><strong>背压控制</strong> - 流量控制机制</li>
                        <li><strong>错误处理</strong> - 完善的异常处理</li>
                    </ul>
                    
                    <p><a href="/api-docs">📖 查看 API 文档</a></p>
                </div>
            </body>
            </html>
            """;
    }
    
    private Object getApiDocumentation() {
        return new Object() {
            public final String title = "Spring WebFlux Demo API";
            public final String version = "1.0.0";
            public final String description = "响应式用户管理 API";
            public final Object endpoints = new Object() {
                public final Object users = new Object() {
                    public final String get = "GET /api/users - 获取用户列表";
                    public final String post = "POST /api/users - 创建用户";
                    public final String getById = "GET /api/users/{id} - 获取用户";
                    public final String put = "PUT /api/users/{id} - 更新用户";
                    public final String delete = "DELETE /api/users/{id} - 删除用户";
                };
                public final Object streams = new Object() {
                    public final String activities = "GET /api/users/stream - 用户活动流";
                    public final String updates = "GET /api/users/sse - 用户更新流";
                };
                public final Object health = new Object() {
                    public final String basic = "GET /actuator/health - 健康检查";
                    public final String detailed = "GET /actuator/health/detailed - 详细检查";
                };
            };
        };
    }
    
    private String getApiDocumentationHtml() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>API 文档 - Spring WebFlux Demo</title>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .container { max-width: 1000px; margin: 0 auto; }
                    .endpoint { background: #f8f9fa; padding: 15px; margin: 10px 0; border-radius: 5px; border-left: 4px solid #007acc; }
                    .method { display: inline-block; padding: 4px 8px; border-radius: 3px; color: white; font-weight: bold; margin-right: 10px; }
                    .get { background-color: #61affe; }
                    .post { background-color: #49cc90; }
                    .put { background-color: #fca130; }
                    .delete { background-color: #f93e3e; }
                    .path { font-family: monospace; color: #333; }
                    .description { color: #666; margin-top: 5px; }
                    h1 { color: #333; }
                    h2 { color: #666; border-bottom: 2px solid #eee; padding-bottom: 10px; }
                    pre { background: #f4f4f4; padding: 10px; border-radius: 3px; overflow-x: auto; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>📖 Spring WebFlux Demo API 文档</h1>
                    
                    <h2>用户管理 API</h2>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/api/users</span></div>
                        <div class="description">获取用户列表，支持分页和搜索</div>
                        <div class="description"><strong>参数:</strong> page, size, search</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/api/users/{id}</span></div>
                        <div class="description">根据ID获取单个用户信息</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method post">POST</span><span class="path">/api/users</span></div>
                        <div class="description">创建新用户</div>
                        <div class="description"><strong>请求体示例:</strong></div>
                        <pre>{"name": "张三", "email": "zhangsan@example.com"}</pre>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method put">PUT</span><span class="path">/api/users/{id}</span></div>
                        <div class="description">更新用户信息</div>
                        <div class="description"><strong>请求体示例:</strong></div>
                        <pre>{"name": "李四", "accountType": "premium"}</pre>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method delete">DELETE</span><span class="path">/api/users/{id}</span></div>
                        <div class="description">删除用户</div>
                    </div>
                    
                    <h2>实时数据流 API</h2>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/api/users/stream</span></div>
                        <div class="description">用户活动实时流 (Server-Sent Events)</div>
                        <div class="description"><strong>Content-Type:</strong> text/event-stream</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/api/users/sse</span></div>
                        <div class="description">用户更新事件流</div>
                        <div class="description"><strong>参数:</strong> userId (可选)</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/api/reactive/users/live-updates</span></div>
                        <div class="description">用户更新实时流 (NDJSON)</div>
                        <div class="description"><strong>Content-Type:</strong> application/x-ndjson</div>
                    </div>
                    
                    <h2>健康检查 API</h2>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/actuator/health</span></div>
                        <div class="description">基础健康检查</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/actuator/health/detailed</span></div>
                        <div class="description">详细健康检查，包含系统指标</div>
                    </div>
                    
                    <div class="endpoint">
                        <div><span class="method get">GET</span><span class="path">/actuator/metrics</span></div>
                        <div class="description">应用指标信息</div>
                    </div>
                    
                    <h2>测试示例</h2>
                    
                    <h3>创建用户</h3>
                    <pre>curl -X POST http://localhost:8080/api/users \\
  -H "Content-Type: application/json" \\
  -d '{"name":"测试用户","email":"test@example.com"}'</pre>
                    
                    <h3>获取用户列表</h3>
                    <pre>curl http://localhost:8080/api/users?page=0&size=10</pre>
                    
                    <h3>监听实时事件</h3>
                    <pre>curl -N http://localhost:8080/api/users/stream</pre>
                    
                    <p><a href="/">← 返回首页</a></p>
                </div>
            </body>
            </html>
            """;
    }
}
