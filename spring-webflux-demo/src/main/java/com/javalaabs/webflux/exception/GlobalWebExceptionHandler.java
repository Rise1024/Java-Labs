package com.javalaabs.webflux.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * 全局Web异常处理器
 * 处理所有未被其他处理器处理的异常
 */
@Component
@Order(-2) // 高优先级
public class GlobalWebExceptionHandler implements WebExceptionHandler {
    
    private final ObjectMapper objectMapper;
    
    public GlobalWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        
        if (response.isCommitted()) {
            return Mono.error(ex);
        }
        
        // 设置响应头
        response.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        
        // 确定HTTP状态码和错误信息
        HttpStatus status = determineHttpStatus(ex);
        ErrorResponse errorResponse = createErrorResponse(ex, status, exchange);
        
        response.setStatusCode(status);
        
        // 序列化错误响应
        String errorJson;
        try {
            errorJson = objectMapper.writeValueAsString(errorResponse);
        } catch (JsonProcessingException e) {
            errorJson = createFallbackErrorJson(ex, status);
        }
        
        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
    
    /**
     * 确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ValidationException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof UserNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else if (ex instanceof DuplicateEmailException) {
            return HttpStatus.CONFLICT;
        } else if (ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN;
        } else if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return HttpStatus.REQUEST_TIMEOUT;
        } else if (ex instanceof java.util.concurrent.RejectedExecutionException) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * 创建错误响应对象
     */
    private ErrorResponse createErrorResponse(Throwable ex, HttpStatus status, ServerWebExchange exchange) {
        String message = getErrorMessage(ex, status);
        String errorCode = getErrorCode(ex);
        String path = exchange.getRequest().getPath().value();
        String requestId = getRequestId(exchange);
        
        return ErrorResponse.builder()
                           .timestamp(Instant.now())
                           .status(status.value())
                           .error(status.getReasonPhrase())
                           .message(message)
                           .code(errorCode)
                           .path(path)
                           .requestId(requestId)
                           .build();
    }
    
    /**
     * 获取错误消息
     */
    private String getErrorMessage(Throwable ex, HttpStatus status) {
        if (ex instanceof BusinessException) {
            return ex.getMessage();
        } else if (status == HttpStatus.INTERNAL_SERVER_ERROR) {
            // 生产环境不暴露内部错误详情
            return "服务器内部错误";
        } else {
            return ex.getMessage() != null ? ex.getMessage() : status.getReasonPhrase();
        }
    }
    
    /**
     * 获取错误代码
     */
    private String getErrorCode(Throwable ex) {
        if (ex instanceof BusinessException) {
            return ((BusinessException) ex).getErrorCode();
        } else if (ex instanceof ValidationException) {
            return "VALIDATION_ERROR";
        } else if (ex instanceof IllegalArgumentException) {
            return "INVALID_ARGUMENT";
        } else if (ex instanceof java.util.concurrent.TimeoutException) {
            return "REQUEST_TIMEOUT";
        } else {
            return "INTERNAL_ERROR";
        }
    }
    
    /**
     * 获取请求ID
     */
    private String getRequestId(ServerWebExchange exchange) {
        Object requestId = exchange.getAttributes().get("requestId");
        return requestId != null ? requestId.toString() : "unknown";
    }
    
    /**
     * 创建备用错误JSON（当序列化失败时使用）
     */
    private String createFallbackErrorJson(Throwable ex, HttpStatus status) {
        Map<String, Object> errorMap = Map.of(
            "timestamp", Instant.now().toString(),
            "status", status.value(),
            "error", status.getReasonPhrase(),
            "message", "序列化错误响应失败",
            "code", "SERIALIZATION_ERROR"
        );
        
        try {
            return objectMapper.writeValueAsString(errorMap);
        } catch (JsonProcessingException e) {
            // 最后的备用方案
            return String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"序列化失败\"}",
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase()
            );
        }
    }
    
    /**
     * 错误响应类
     */
    public static class ErrorResponse {
        private Instant timestamp;
        private int status;
        private String error;
        private String message;
        private String code;
        private String path;
        private String requestId;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final ErrorResponse response = new ErrorResponse();
            
            public Builder timestamp(Instant timestamp) {
                response.timestamp = timestamp;
                return this;
            }
            
            public Builder status(int status) {
                response.status = status;
                return this;
            }
            
            public Builder error(String error) {
                response.error = error;
                return this;
            }
            
            public Builder message(String message) {
                response.message = message;
                return this;
            }
            
            public Builder code(String code) {
                response.code = code;
                return this;
            }
            
            public Builder path(String path) {
                response.path = path;
                return this;
            }
            
            public Builder requestId(String requestId) {
                response.requestId = requestId;
                return this;
            }
            
            public ErrorResponse build() {
                return response;
            }
        }
        
        // Getters
        public Instant getTimestamp() { return timestamp; }
        public int getStatus() { return status; }
        public String getError() { return error; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public String getPath() { return path; }
        public String getRequestId() { return requestId; }
    }
}
