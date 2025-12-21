package com.dataelf.platform.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis缓存配置
 * 
 * 缓存策略：
 * - ai-content-jsonld: 已发布内容的JSON-LD (TTL: 1小时)
 * - ai-search-results: AI搜索结果 (TTL: 5分钟)
 * - ai-sitemap: 网站地图 (TTL: 1小时)
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 创建ObjectMapper用于序列化
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            LaissezFaireSubTypeValidator.instance,
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );
        
        // 配置序列化器
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
            .disableCachingNullValues();
        
        // 针对不同缓存的特定配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // AI内容JSON-LD缓存 - 1小时
        cacheConfigurations.put("ai-content-jsonld", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // AI搜索结果缓存 - 5分钟
        cacheConfigurations.put("ai-search-results", 
            defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        // AI网站地图缓存 - 1小时
        cacheConfigurations.put("ai-sitemap", 
            defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 用户会话缓存 - 24小时
        cacheConfigurations.put("user-sessions", 
            defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // 模板定义缓存 - 永久（手动失效）
        cacheConfigurations.put("template-definitions", 
            defaultConfig.entryTtl(Duration.ofDays(365)));
        
        // 首页内容列表缓存 - 5分钟
        cacheConfigurations.put("homepage-contents", 
            defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
}
