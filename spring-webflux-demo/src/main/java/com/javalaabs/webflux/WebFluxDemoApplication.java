package com.javalaabs.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;

/**
 * Spring WebFlux 演示应用启动类
 */
@SpringBootApplication
public class WebFluxDemoApplication implements ApplicationListener<ApplicationReadyEvent> {
    
    public static void main(String[] args) {
        SpringApplication.run(WebFluxDemoApplication.class, args);
    }
    
    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment environment = event.getApplicationContext().getEnvironment();
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        
        System.out.println("🚀 Spring WebFlux 演示应用启动成功！");
        System.out.println("📖 应用访问地址:");
        System.out.println("   - 主页: http://localhost:" + port + contextPath + "/");
        System.out.println("   - API文档: http://localhost:" + port + contextPath + "/api");
        System.out.println("   - 健康检查: http://localhost:" + port + contextPath + "/actuator/health");
        System.out.println("   - 指标信息: http://localhost:" + port + contextPath + "/actuator/metrics");
        System.out.println();
        System.out.println("📋 API 端点示例:");
        System.out.println("   - 用户列表: http://localhost:" + port + contextPath + "/api/users");
        System.out.println("   - 创建用户: POST http://localhost:" + port + contextPath + "/api/users");
        System.out.println("   - 实时流: http://localhost:" + port + contextPath + "/api/users/stream");
        System.out.println("   - SSE事件: http://localhost:" + port + contextPath + "/api/users/sse");
        System.out.println();
        System.out.println("🎯 特性演示:");
        System.out.println("   ✅ 响应式编程 (Reactor)");
        System.out.println("   ✅ 非阻塞I/O");
        System.out.println("   ✅ 服务器推送事件 (SSE)");
        System.out.println("   ✅ 函数式路由");
        System.out.println("   ✅ WebClient 客户端");
        System.out.println("   ✅ 错误处理");
        System.out.println("   ✅ 性能监控");
        System.out.println("   ✅ 健康检查");
        System.out.println();
        System.out.println("📚 基于博文: Spring WebFlux 响应式编程详解");
        System.out.println("═══════════════════════════════════════════════════");
    }
}
