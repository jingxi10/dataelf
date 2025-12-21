package com.dataelf.platform.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Prometheus指标配置
 * 定义自定义业务指标
 */
@Configuration
public class MetricsConfig {

    /**
     * 用户注册计数器
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry registry) {
        return Counter.builder("user.registration.total")
                .description("Total number of user registrations")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 用户登录计数器
     */
    @Bean
    public Counter userLoginCounter(MeterRegistry registry) {
        return Counter.builder("user.login.total")
                .description("Total number of successful user logins")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 登录失败计数器
     */
    @Bean
    public Counter loginFailureCounter(MeterRegistry registry) {
        return Counter.builder("user.login.failure.total")
                .description("Total number of failed login attempts")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 内容创建计数器
     */
    @Bean
    public Counter contentCreationCounter(MeterRegistry registry) {
        return Counter.builder("content.creation.total")
                .description("Total number of contents created")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 内容发布计数器
     */
    @Bean
    public Counter contentPublishCounter(MeterRegistry registry) {
        return Counter.builder("content.publish.total")
                .description("Total number of contents published")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 内容审核批准计数器
     */
    @Bean
    public Counter contentApprovalCounter(MeterRegistry registry) {
        return Counter.builder("content.approval.total")
                .description("Total number of contents approved")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 内容审核拒绝计数器
     */
    @Bean
    public Counter contentRejectionCounter(MeterRegistry registry) {
        return Counter.builder("content.rejection.total")
                .description("Total number of contents rejected")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * AI API调用计数器
     */
    @Bean
    public Counter aiApiCallCounter(MeterRegistry registry) {
        return Counter.builder("ai.api.call.total")
                .description("Total number of AI API calls")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 用户API调用计数器
     */
    @Bean
    public Counter userApiCallCounter(MeterRegistry registry) {
        return Counter.builder("user.api.call.total")
                .description("Total number of user API calls")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 邮件发送计数器
     */
    @Bean
    public Counter emailSentCounter(MeterRegistry registry) {
        return Counter.builder("email.sent.total")
                .description("Total number of emails sent")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 邮件发送失败计数器
     */
    @Bean
    public Counter emailFailureCounter(MeterRegistry registry) {
        return Counter.builder("email.failure.total")
                .description("Total number of email sending failures")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 缓存命中计数器
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cache.hit.total")
                .description("Total number of cache hits")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 缓存未命中计数器
     */
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cache.miss.total")
                .description("Total number of cache misses")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * JSON-LD生成时间计时器
     */
    @Bean
    public Timer jsonLdGenerationTimer(MeterRegistry registry) {
        return Timer.builder("jsonld.generation.time")
                .description("Time taken to generate JSON-LD")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 内容导出时间计时器
     */
    @Bean
    public Timer contentExportTimer(MeterRegistry registry) {
        return Timer.builder("content.export.time")
                .description("Time taken to export content")
                .tag("application", "ai-data-platform")
                .register(registry);
    }

    /**
     * 数据库查询时间计时器
     */
    @Bean
    public Timer databaseQueryTimer(MeterRegistry registry) {
        return Timer.builder("database.query.time")
                .description("Time taken for database queries")
                .tag("application", "ai-data-platform")
                .register(registry);
    }
}
