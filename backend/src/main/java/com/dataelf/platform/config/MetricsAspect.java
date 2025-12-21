package com.dataelf.platform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 指标收集切面
 * 自动收集业务操作的Prometheus指标
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final MeterRegistry meterRegistry;

    /**
     * 记录用户注册指标
     */
    @Around("execution(* com.dataelf.platform.service.UserService.register(..))")
    public Object trackUserRegistration(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("user.registration.total");
        try {
            Object result = joinPoint.proceed();
            counter.increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("user.registration.failure.total").increment();
            throw e;
        }
    }

    /**
     * 记录用户登录指标
     */
    @Around("execution(* com.dataelf.platform.service.UserService.login(..))")
    public Object trackUserLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            meterRegistry.counter("user.login.total").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("user.login.failure.total").increment();
            throw e;
        }
    }

    /**
     * 记录内容创建指标
     */
    @Around("execution(* com.dataelf.platform.service.ContentService.createContent(..))")
    public Object trackContentCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("content.creation.total");
        Timer timer = meterRegistry.timer("content.creation.time");
        
        return timer.record(() -> {
            try {
                Object result = joinPoint.proceed();
                counter.increment();
                return result;
            } catch (Throwable e) {
                meterRegistry.counter("content.creation.failure.total").increment();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 记录内容发布指标
     */
    @Around("execution(* com.dataelf.platform.service.ContentService.publishContent(..))")
    public Object trackContentPublish(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("content.publish.total");
        try {
            Object result = joinPoint.proceed();
            counter.increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("content.publish.failure.total").increment();
            throw e;
        }
    }

    /**
     * 记录内容批准指标
     */
    @Around("execution(* com.dataelf.platform.service.ContentService.approveContent(..))")
    public Object trackContentApproval(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("content.approval.total");
        try {
            Object result = joinPoint.proceed();
            counter.increment();
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 记录内容拒绝指标
     */
    @Around("execution(* com.dataelf.platform.service.ContentService.rejectContent(..))")
    public Object trackContentRejection(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("content.rejection.total");
        try {
            Object result = joinPoint.proceed();
            counter.increment();
            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 记录AI API调用指标
     */
    @Around("execution(* com.dataelf.platform.controller.AiApiController.*(..))")
    public Object trackAiApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("ai.api.call.total");
        Timer timer = meterRegistry.timer("ai.api.response.time");
        
        return timer.record(() -> {
            try {
                Object result = joinPoint.proceed();
                counter.increment();
                return result;
            } catch (Throwable e) {
                meterRegistry.counter("ai.api.error.total").increment();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 记录用户API调用指标
     */
    @Around("execution(* com.dataelf.platform.controller.UserApiController.*(..))")
    public Object trackUserApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        Counter counter = meterRegistry.counter("user.api.call.total");
        Timer timer = meterRegistry.timer("user.api.response.time");
        
        return timer.record(() -> {
            try {
                Object result = joinPoint.proceed();
                counter.increment();
                return result;
            } catch (Throwable e) {
                meterRegistry.counter("user.api.error.total").increment();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 记录JSON-LD生成指标
     */
    @Around("execution(* com.dataelf.platform.service.ContentService.generateJsonLd(..))")
    public Object trackJsonLdGeneration(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer timer = meterRegistry.timer("jsonld.generation.time");
        
        return timer.record(() -> {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                meterRegistry.counter("jsonld.generation.failure.total").increment();
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 记录内容导出指标
     */
    @Around("execution(* com.dataelf.platform.service.ExportService.*(..))")
    public Object trackContentExport(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Timer timer = meterRegistry.timer("content.export.time", "format", methodName);
        
        return timer.record(() -> {
            try {
                Object result = joinPoint.proceed();
                meterRegistry.counter("content.export.total", "format", methodName).increment();
                return result;
            } catch (Throwable e) {
                meterRegistry.counter("content.export.failure.total", "format", methodName).increment();
                throw new RuntimeException(e);
            }
        });
    }
}
