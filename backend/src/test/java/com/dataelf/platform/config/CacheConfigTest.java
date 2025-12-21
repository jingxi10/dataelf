package com.dataelf.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis缓存配置测试
 * 
 * 验证需求: 性能优化 - Redis缓存配置
 */
class CacheConfigTest {
    
    @Test
    void testCacheConfigurationCreatesAllRequiredCaches() {
        // Create mock Redis connection factory
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);
        
        // Create CacheConfig instance
        CacheConfig cacheConfig = new CacheConfig();
        
        // Get the cache manager
        CacheManager cacheManager = cacheConfig.cacheManager(connectionFactory);
        
        assertNotNull(cacheManager, "CacheManager should be configured");
        assertTrue(cacheManager instanceof RedisCacheManager, "CacheManager should be RedisCacheManager");
    }
    
    @Test
    void testAllRequiredCachesAreConfigured() {
        // Create mock Redis connection factory
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);
        
        // Create CacheConfig instance
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager(connectionFactory);
        
        // Verify all caches from task requirements are configured
        String[] requiredCaches = {
            "ai-content-jsonld",      // 内容JSON-LD缓存 - TTL: 1小时
            "user-sessions",          // 用户会话缓存 - TTL: 24小时
            "template-definitions",   // 模板定义缓存 - TTL: 永久（365天）
            "homepage-contents",      // 首页内容列表缓存 - TTL: 5分钟
            "ai-search-results",      // AI搜索结果缓存 - TTL: 5分钟
            "ai-sitemap"              // AI网站地图缓存 - TTL: 1小时
        };
        
        for (String cacheName : requiredCaches) {
            assertNotNull(cacheManager.getCache(cacheName), 
                "Cache '" + cacheName + "' should be configured");
        }
    }
    
    @Test
    void testCacheNamesMatchTaskRequirements() {
        // Verify cache names match the task requirements exactly
        RedisConnectionFactory connectionFactory = Mockito.mock(RedisConnectionFactory.class);
        CacheConfig cacheConfig = new CacheConfig();
        CacheManager cacheManager = cacheConfig.cacheManager(connectionFactory);
        
        // Task requirement 1: 配置内容JSON-LD缓存
        assertNotNull(cacheManager.getCache("ai-content-jsonld"));
        
        // Task requirement 2: 配置用户会话缓存
        assertNotNull(cacheManager.getCache("user-sessions"));
        
        // Task requirement 3: 配置模板定义缓存
        assertNotNull(cacheManager.getCache("template-definitions"));
        
        // Task requirement 4: 配置首页内容列表缓存
        assertNotNull(cacheManager.getCache("homepage-contents"));
    }
}
