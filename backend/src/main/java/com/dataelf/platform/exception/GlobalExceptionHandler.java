package com.dataelf.platform.exception;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final MeterRegistry meterRegistry;
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        incrementErrorCounter("VALIDATION_ERROR");
        log.error("Validation error - RequestId: {}, UserId: {}, Message: {}", 
            MDC.get("requestId"), MDC.get("userId"), ex.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "VALIDATION_ERROR");
        error.put("message", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        incrementErrorCounter("AUTHENTICATION_ERROR");
        log.error("Authentication error - RequestId: {}, IP: {}, Message: {}", 
            MDC.get("requestId"), MDC.get("ipAddress"), ex.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "AUTHENTICATION_ERROR");
        error.put("message", ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex) {
        incrementErrorCounter("ACCESS_DENIED");
        log.error("Access denied - RequestId: {}, UserId: {}, Message: {}", 
            MDC.get("requestId"), MDC.get("userId"), ex.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "ACCESS_DENIED");
        error.put("message", "访问被拒绝");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
        incrementErrorCounter("NOT_FOUND");
        log.error("Entity not found - RequestId: {}, Message: {}", 
            MDC.get("requestId"), ex.getMessage());
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "NOT_FOUND");
        error.put("message", "资源不存在");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        incrementErrorCounter("VALIDATION_ERROR");
        
        List<Map<String, String>> details = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> detail = new HashMap<>();
            detail.put("field", error.getField());
            detail.put("message", error.getDefaultMessage());
            details.add(detail);
        }
        
        log.error("Validation error - RequestId: {}, Fields: {}", 
            MDC.get("requestId"), details);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "VALIDATION_ERROR");
        error.put("message", "输入验证失败");
        error.put("details", details);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        incrementErrorCounter("INTERNAL_ERROR");
        log.error("Unexpected error - RequestId: {}, UserId: {}, Operation: {}, Exception: ", 
            MDC.get("requestId"), MDC.get("userId"), MDC.get("operation"), ex);
        
        Map<String, Object> error = new HashMap<>();
        error.put("code", "INTERNAL_ERROR");
        error.put("message", "服务器内部错误: " + ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", error);
        response.put("timestamp", LocalDateTime.now());
        response.put("requestId", MDC.get("requestId"));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * 增加错误计数器
     */
    private void incrementErrorCounter(String errorType) {
        Counter counter = meterRegistry.counter("application.errors.total", "type", errorType);
        counter.increment();
    }
}
