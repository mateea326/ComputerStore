package com.example.ComputerStore.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    // Configurații avansate pentru Redis pot fi adăugate aici (TTL, serializare),
    // dar setările default (folosind StringRedisSerializer / JdkSerialization) sunt suficiente pentru cerință.
}
