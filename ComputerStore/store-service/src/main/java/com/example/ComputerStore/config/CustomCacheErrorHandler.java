package com.example.ComputerStore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.stereotype.Component;

@Component
public class CustomCacheErrorHandler implements CacheErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache get error for cache '{}', key '{}'. Falling back to database.", cache != null ? cache.getName() : "unknown", key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        log.warn("Cache put error for cache '{}', key '{}'. Ignored.", cache != null ? cache.getName() : "unknown", key);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        log.warn("Cache evict error for cache '{}', key '{}'. Ignored.", cache != null ? cache.getName() : "unknown", key);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        log.warn("Cache clear error for cache '{}'. Ignored.", cache != null ? cache.getName() : "unknown");
    }
}
