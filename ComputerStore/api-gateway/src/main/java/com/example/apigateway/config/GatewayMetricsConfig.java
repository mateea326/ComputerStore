package com.example.apigateway.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Configuratie metrici custom pentru API Gateway.
 * Expuse la /actuator/prometheus si colectate de Prometheus.
 */
@Configuration
public class GatewayMetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(GatewayMetricsConfig.class);

    /**
     * Counter: numarul total de request-uri rutate prin gateway, pe serviciu.
     */
    @Bean
    public Counter gatewayTotalRequestsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("gateway.requests.total")
                .description("Total number of requests routed through the API Gateway")
                .register(meterRegistry);
    }

    /**
     * GlobalFilter care inregistreaza metrici per ruta.
     * Implementeaza GlobalFilter + Ordered pentru a seta prioritatea corect.
     */
    @Bean
    public GlobalFilter metricsFilter(MeterRegistry meterRegistry) {
        return new MetricsGlobalFilter(meterRegistry);
    }

    /**
     * Clasa interna statica pentru a putea implementa atat GlobalFilter cat si Ordered.
     */
    private static class MetricsGlobalFilter implements GlobalFilter, Ordered {

        private final MeterRegistry meterRegistry;

        MetricsGlobalFilter(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();

            // Identificam serviciul tinta din path
            String targetService = "unknown";
            if (path.startsWith("/api/store")) {
                targetService = "store-service";
            } else if (path.startsWith("/api/user")) {
                targetService = "user-service";
            }

            final String service = targetService;
            long startTime = System.currentTimeMillis();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int statusCode = exchange.getResponse().getStatusCode() != null
                        ? exchange.getResponse().getStatusCode().value()
                        : 0;

                // Counter per serviciu + metoda HTTP
                Counter.builder("gateway.requests.routed")
                        .description("Requests routed to downstream services")
                        .tag("service", service)
                        .tag("method", method)
                        .tag("status", String.valueOf(statusCode))
                        .register(meterRegistry)
                        .increment();

                // Timer pentru latency cu percentile
                Timer.builder("gateway.request.duration")
                        .description("Duration of requests through gateway")
                        .tag("service", service)
                        .tag("method", method)
                        .publishPercentiles(0.5, 0.95, 0.99)
                        .publishPercentileHistogram()
                        .register(meterRegistry)
                        .record(java.time.Duration.ofMillis(duration));
            }));
        }

        @Override
        public int getOrder() {
            return Ordered.HIGHEST_PRECEDENCE + 1;
        }
    }
}
