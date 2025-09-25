package com.javalaabs.webflux.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 响应式控制器异常处理
 * 专门处理控制器层的异常
 */
@RestControllerAdvice
public class ReactiveControllerAdvice {
    
    /**
     * 处理验证异常
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ValidationErrorResponse>> handleValidationErrors(WebExchangeBindException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        
        Map<String, String> errors = fieldErrors.stream()
            .collect(Collectors.toMap(
                FieldError::getField,
                FieldError::getDefaultMessage,
                (existing, replacement) -> existing // 如果字段重复，保留第一个错误
            ));
        
        ValidationErrorResponse errorResponse = ValidationErrorResponse.builder()
                                                                      .message("验证失败")
                                                                      .fieldErrors(errors)
                                                                      .timestamp(Instant.now())
                                                                      .build();
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * 处理输入异常
     */
    @ExceptionHandler(ServerWebInputException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInputError(ServerWebInputException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message("输入参数错误: " + ex.getReason())
                                                  .code("INPUT_ERROR")
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * 处理响应状态异常
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleResponseStatusException(ResponseStatusException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message(ex.getReason())
                                                  .code("RESPONSE_STATUS_ERROR")
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleBusinessException(BusinessException ex) {
        HttpStatus status = determineHttpStatus(ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message(ex.getMessage())
                                                  .code(ex.getErrorCode())
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.status(status).body(errorResponse));
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message(ex.getMessage())
                                                  .code("INVALID_ARGUMENT")
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
    
    /**
     * 处理安全异常
     */
    @ExceptionHandler(SecurityException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleSecurityException(SecurityException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message("访问被拒绝")
                                                  .code("ACCESS_DENIED")
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse));
    }
    
    /**
     * 处理超时异常
     */
    @ExceptionHandler(java.util.concurrent.TimeoutException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleTimeoutException(java.util.concurrent.TimeoutException ex) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                                                  .message("请求超时")
                                                  .code("REQUEST_TIMEOUT")
                                                  .timestamp(Instant.now())
                                                  .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse));
    }
    
    /**
     * 根据业务异常类型确定HTTP状态码
     */
    private HttpStatus determineHttpStatus(BusinessException ex) {
        String errorCode = ex.getErrorCode();
        
        switch (errorCode) {
            case "USER_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
            case "DUPLICATE_EMAIL":
                return HttpStatus.CONFLICT;
            case "VALIDATION_ERROR":
                return HttpStatus.BAD_REQUEST;
            case "ACCESS_DENIED":
                return HttpStatus.FORBIDDEN;
            case "UNAUTHORIZED":
                return HttpStatus.UNAUTHORIZED;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * 验证错误响应类
     */
    public static class ValidationErrorResponse {
        private String message;
        private Map<String, String> fieldErrors;
        private Instant timestamp;
        private String requestId;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final ValidationErrorResponse response = new ValidationErrorResponse();
            
            public Builder message(String message) {
                response.message = message;
                return this;
            }
            
            public Builder fieldErrors(Map<String, String> fieldErrors) {
                response.fieldErrors = fieldErrors;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                response.timestamp = timestamp;
                return this;
            }
            
            public Builder requestId(String requestId) {
                response.requestId = requestId;
                return this;
            }
            
            public ValidationErrorResponse build() {
                return response;
            }
        }
        
        // Getters
        public String getMessage() { return message; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }
        public Instant getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
    }
    
    /**
     * 通用错误响应类
     */
    public static class ErrorResponse {
        private String message;
        private String code;
        private Instant timestamp;
        private String requestId;
        
        public static Builder builder() {
            return new Builder();
        }
        
        public static class Builder {
            private final ErrorResponse response = new ErrorResponse();
            
            public Builder message(String message) {
                response.message = message;
                return this;
            }
            
            public Builder code(String code) {
                response.code = code;
                return this;
            }
            
            public Builder timestamp(Instant timestamp) {
                response.timestamp = timestamp;
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
        public String getMessage() { return message; }
        public String getCode() { return code; }
        public Instant getTimestamp() { return timestamp; }
        public String getRequestId() { return requestId; }
    }
}
