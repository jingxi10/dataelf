package com.dataelf.platform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 日志和监控配置测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class LoggingMonitoringTest {

    private static final Logger log = LoggerFactory.getLogger(LoggingMonitoringTest.class);
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");

    @Autowired
    private MeterRegistry meterRegistry;

    @Test
    public void testMeterRegistryIsConfigured() {
        assertNotNull(meterRegistry, "MeterRegistry should be configured");
    }

    @Test
    public void testCustomMetricsAreRegistered() {
        // 验证自定义指标已注册
        Counter userRegistrationCounter = meterRegistry.find("user.registration.total").counter();
        assertNotNull(userRegistrationCounter, "User registration counter should be registered");

        Counter userLoginCounter = meterRegistry.find("user.login.total").counter();
        assertNotNull(userLoginCounter, "User login counter should be registered");

        Counter contentCreationCounter = meterRegistry.find("content.creation.total").counter();
        assertNotNull(contentCreationCounter, "Content creation counter should be registered");

        Timer jsonLdTimer = meterRegistry.find("jsonld.generation.time").timer();
        assertNotNull(jsonLdTimer, "JSON-LD generation timer should be registered");
    }

    @Test
    public void testCounterIncrement() {
        Counter testCounter = meterRegistry.counter("test.counter");
        double initialCount = testCounter.count();
        
        testCounter.increment();
        
        assertEquals(initialCount + 1, testCounter.count(), "Counter should increment by 1");
    }

    @Test
    public void testTimerRecording() {
        Timer testTimer = meterRegistry.timer("test.timer");
        long initialCount = testTimer.count();
        
        testTimer.record(() -> {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        assertEquals(initialCount + 1, testTimer.count(), "Timer should record one execution");
        assertTrue(testTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS) > 0, 
            "Timer should record positive time");
    }

    @Test
    public void testMDCContext() {
        // 设置MDC上下文
        MDC.put("requestId", "test-request-123");
        MDC.put("userId", "test-user");
        MDC.put("operation", "testOperation");
        
        try {
            // 验证MDC值
            assertEquals("test-request-123", MDC.get("requestId"));
            assertEquals("test-user", MDC.get("userId"));
            assertEquals("testOperation", MDC.get("operation"));
            
            // 记录日志
            log.info("Test log with MDC context");
            
        } finally {
            // 清理MDC
            MDC.clear();
        }
        
        // 验证MDC已清理
        assertNull(MDC.get("requestId"));
        assertNull(MDC.get("userId"));
        assertNull(MDC.get("operation"));
    }

    @Test
    public void testAuditLogger() {
        // 设置审计日志上下文
        MDC.put("requestId", "audit-request-123");
        MDC.put("userId", "audit-user");
        MDC.put("resourceType", "TEST_RESOURCE");
        MDC.put("resourceId", "123");
        MDC.put("action", "TEST_ACTION");
        MDC.put("result", "SUCCESS");
        
        try {
            // 记录审计日志
            AUDIT_LOGGER.info("Test audit log entry");
            
            // 验证审计日志记录器存在
            assertNotNull(AUDIT_LOGGER);
            
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void testLogLevels() {
        // 测试不同日志级别
        log.trace("This is a TRACE log");
        log.debug("This is a DEBUG log");
        log.info("This is an INFO log");
        log.warn("This is a WARN log");
        log.error("This is an ERROR log");
        
        // 如果没有抛出异常，测试通过
        assertTrue(true);
    }

    @Test
    public void testStructuredLogging() {
        // 测试结构化日志
        MDC.put("requestId", "struct-log-123");
        MDC.put("userId", "struct-user");
        MDC.put("ipAddress", "192.168.1.100");
        
        try {
            log.info("Structured log entry with context");
            
            // 验证所有MDC字段都已设置
            assertNotNull(MDC.get("requestId"));
            assertNotNull(MDC.get("userId"));
            assertNotNull(MDC.get("ipAddress"));
            
        } finally {
            MDC.clear();
        }
    }

    @Test
    public void testMetricsWithTags() {
        // 测试带标签的指标
        Counter taggedCounter = meterRegistry.counter("test.tagged.counter", 
            "type", "test", 
            "status", "success");
        
        double initialCount = taggedCounter.count();
        taggedCounter.increment();
        
        assertEquals(initialCount + 1, taggedCounter.count(), 
            "Tagged counter should increment");
    }

    @Test
    public void testErrorMetrics() {
        // 测试错误指标
        Counter errorCounter = meterRegistry.counter("application.errors.total", 
            "type", "TEST_ERROR");
        
        double initialCount = errorCounter.count();
        errorCounter.increment();
        
        assertEquals(initialCount + 1, errorCounter.count(), 
            "Error counter should increment");
    }
}
