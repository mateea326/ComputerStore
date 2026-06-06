package com.example.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Configuratie API Gateway:
 * - Routes programatice pentru store-service si user-service
 * - Key Resolver pentru rate limiting (identificare per IP)
 * - CORS global configuration
 */
@Configuration
public class GatewayConfig {

    /**
     * KeyResolver pentru Rate Limiting.
     * Identifica clientul dupa IP (sau dupa header Authorization daca exista).
     * Folosit de RequestRateLimiterGatewayFilterFactory din application.yml.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();

            // Preferam X-Forwarded-For (client real in spatele unui proxy)
            String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                return Mono.just(forwardedFor.split(",")[0].trim());
            }

            // Fallback la IP direct
            if (request.getRemoteAddress() != null) {
                return Mono.just(request.getRemoteAddress().getAddress().getHostAddress());
            }

            return Mono.just("anonymous");
        };
    }

    /**
     * CORS Configuration - permite accesul din browsere la API Gateway.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOriginPatterns(List.of("*"));
        corsConfig.setMaxAge(3600L);
        corsConfig.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        corsConfig.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "X-Request-Id"
        ));
        corsConfig.setExposedHeaders(Arrays.asList(
                "X-Response-Time",
                "X-Processed-By",
                "X-Request-Id",
                "X-RateLimit-Remaining"
        ));
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);
        return source;
    }
}
