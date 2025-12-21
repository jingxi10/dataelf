package com.dataelf.platform.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 审计日志切面
 * 记录所有敏感操作，包括用户ID、操作类型、资源信息、IP地址等
 */
@Aspect
@Component
@Slf4j
public class AuditLoggingAspect {

    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

    /**
     * 在控制器方法执行前设置MDC上下文
     */
    @Before("execution(* com.dataelf.platform.controller..*(..))")
    public void beforeControllerMethod(JoinPoint joinPoint) {
        try {
            // 生成请求ID
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);

            // 获取用户信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                MDC.put("userId", authentication.getName());
            }

            // 获取IP地址
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String ipAddress = getClientIpAddress(request);
                MDC.put("ipAddress", ipAddress);
            }

            // 记录操作信息
            String operation = joinPoint.getSignature().toShortString();
            MDC.put("operation", operation);

        } catch (Exception e) {
            log.error("Error setting up MDC context", e);
        }
    }

    /**
     * 在控制器方法执行后清理MDC上下文
     */
    @After("execution(* com.dataelf.platform.controller..*(..))")
    public void afterControllerMethod() {
        MDC.clear();
    }

    /**
     * 审计用户注册操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.UserService.register(..))", returning = "result")
    public void auditUserRegistration(JoinPoint joinPoint, Object result) {
        try {
            MDC.put("resourceType", "USER");
            MDC.put("action", "REGISTER");
            MDC.put("result", "SUCCESS");
            
            AUDIT_LOGGER.info("User registration completed");
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计用户登录操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.UserService.login(..))", returning = "result")
    public void auditUserLogin(JoinPoint joinPoint, Object result) {
        try {
            MDC.put("resourceType", "USER");
            MDC.put("action", "LOGIN");
            MDC.put("result", "SUCCESS");
            
            AUDIT_LOGGER.info("User login successful");
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计登录失败操作
     */
    @AfterThrowing(pointcut = "execution(* com.dataelf.platform.service.UserService.login(..))", throwing = "ex")
    public void auditLoginFailure(JoinPoint joinPoint, Exception ex) {
        try {
            MDC.put("resourceType", "USER");
            MDC.put("action", "LOGIN");
            MDC.put("result", "FAILURE");
            
            AUDIT_LOGGER.warn("User login failed: {}", ex.getMessage());
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计用户批准操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.UserService.approveUser(..))")
    public void auditUserApproval(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "USER");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "APPROVE");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("User account approved");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计内容提交审核操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.ContentService.submitForReview(..))")
    public void auditContentSubmission(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "CONTENT");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "SUBMIT_REVIEW");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("Content submitted for review");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计内容批准操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.ContentService.approveContent(..))")
    public void auditContentApproval(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "CONTENT");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "APPROVE");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("Content approved");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计内容拒绝操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.ContentService.rejectContent(..))")
    public void auditContentRejection(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "CONTENT");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "REJECT");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("Content rejected");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计内容发布操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.ContentService.publishContent(..))")
    public void auditContentPublish(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "CONTENT");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "PUBLISH");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("Content published");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计模板删除操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.TemplateService.deleteTemplate(..))")
    public void auditTemplateDelete(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "TEMPLATE");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "DELETE");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("Template deleted");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 审计账号延长操作
     */
    @AfterReturning(pointcut = "execution(* com.dataelf.platform.service.UserService.extendAccount(..))")
    public void auditAccountExtension(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                MDC.put("resourceType", "USER");
                MDC.put("resourceId", String.valueOf(args[0]));
                MDC.put("action", "EXTEND_ACCOUNT");
                MDC.put("result", "SUCCESS");
                
                AUDIT_LOGGER.info("User account extended");
            }
        } finally {
            clearAuditMDC();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // 取第一个IP（可能有多个代理）
                if (ip.contains(",")) {
                    ip = ip.split(",")[0];
                }
                return ip.trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * 清理审计相关的MDC字段
     */
    private void clearAuditMDC() {
        MDC.remove("resourceType");
        MDC.remove("resourceId");
        MDC.remove("action");
        MDC.remove("result");
    }
}
