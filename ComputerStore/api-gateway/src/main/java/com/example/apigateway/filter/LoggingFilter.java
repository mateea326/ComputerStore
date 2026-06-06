package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Global Logging Filter - se executa pentru FIECARE request primit de Gateway.
 * <p>
 * Request filtering: adauga headerele X-Request-Id si X-Gateway-Source
 * Response filtering: adauga X-Response-Time si X-Processed-By
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        // Generam un ID unic per request pentru tracing manual
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long startTime = System.currentTimeMillis();

        // -- REQUEST FILTER ----------------------------------------------------
        // Adaugam headerele de request
        ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .header("X-Gateway-Source", "spring-cloud-gateway")
                .build();

        log.info("[GATEWAY] [{}] -> {} {} | IP: {} | Headers: Content-Type={}",
                requestId,
                request.getMethod(),
                request.getURI(),
                clientIp,
                request.getHeaders().getContentType());

        ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

        // -- RESPONSE FILTER ---------------------------------------------------
        return chain.filter(modifiedExchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = exchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;

            // Adaugam headerele de response
            response.getHeaders().add("X-Response-Time", duration + "ms");
            response.getHeaders().add("X-Processed-By", "api-gateway");

            log.info("[GATEWAY] [{}] <- {} {} | Status: {} | Duration: {}ms",
                    requestId,
                    request.getMethod(),
                    request.getURI(),
                    response.getStatusCode(),
                    duration);
        }));
    }

    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        if (request.getRemoteAddress() != null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return "unknown";
    }

    @Override
    public int getOrder() {
        // Prioritate maxima - se executa primul
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
