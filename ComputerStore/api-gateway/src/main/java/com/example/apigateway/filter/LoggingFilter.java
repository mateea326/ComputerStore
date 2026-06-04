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
 * Global Logging Filter — se execută pentru FIECARE request primit de Gateway.
 * <p>
 * Request filtering: adaugă headerele X-Request-Id și X-Gateway-Source
 * Response filtering: adaugă X-Response-Time și X-Processed-By
 */
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Generăm un ID unic per request pentru tracing manual
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        long startTime = System.currentTimeMillis();

        // ── REQUEST FILTER ────────────────────────────────────────────────────
        // Adăugăm headerele de request
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Request-Id", requestId)
                .header("X-Gateway-Source", "ComputerStore-Gateway")
                .header("X-Request-Timestamp", LocalDateTime.now().format(FORMATTER))
                .build();

        log.info("[GATEWAY] [{}] → {} {} | IP: {} | Headers: Content-Type={}",
                requestId,
                request.getMethod(),
                request.getURI().getPath(),
                getClientIp(request),
                request.getHeaders().getFirst("Content-Type"));

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        // ── RESPONSE FILTER ───────────────────────────────────────────────────
        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            ServerHttpResponse response = mutatedExchange.getResponse();
            long duration = System.currentTimeMillis() - startTime;

            // Adăugăm headerele de response
            response.getHeaders().add("X-Response-Time", duration + "ms");
            response.getHeaders().add("X-Processed-By", "ComputerStore-API-Gateway");
            response.getHeaders().add("X-Request-Id", requestId);

            log.info("[GATEWAY] [{}] ← {} {} | Status: {} | Duration: {}ms",
                    requestId,
                    request.getMethod(),
                    request.getURI().getPath(),
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
        // Prioritate maximă — se execută primul
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
