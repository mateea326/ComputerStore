package com.example.ComputerStore.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Autowired
    private CustomCacheErrorHandler customCacheErrorHandler;

    @Override
    public CacheErrorHandler errorHandler() {
        return customCacheErrorHandler;
    }
}
