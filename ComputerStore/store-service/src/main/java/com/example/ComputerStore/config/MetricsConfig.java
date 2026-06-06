package com.example.ComputerStore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuratie metrici custom pentru store-service.
 * Toate metricile sunt automat expuse la /actuator/prometheus
 * si colectate de Prometheus.
 */
@Configuration
public class MetricsConfig {

    // Gauge: numarul curent de produse active in magazin
    private final AtomicInteger activeProductsCount = new AtomicInteger(0);

    /**
     * Counter: comenzi plasate cu succes.
     * Etichetat cu tipul de plata pentru analiza.
     */
    @Bean
    public Counter ordersPlacedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("store.orders.placed.total")
                .description("Total number of successfully placed orders")
                .tag("service", "store-service")
                .register(meterRegistry);
    }

    /**
     * Counter: produse adaugate in cos.
     */
    @Bean
    public Counter cartAddedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("store.cart.items.added.total")
                .description("Total number of items added to cart")
                .tag("service", "store-service")
                .register(meterRegistry);
    }

    /**
     * Counter: produse adaugate/scoase din wishlist.
     */
    @Bean
    public Counter wishlistOperationsCounter(MeterRegistry meterRegistry) {
        return Counter.builder("store.wishlist.operations.total")
                .description("Total number of wishlist operations (add/remove)")
                .tag("service", "store-service")
                .register(meterRegistry);
    }

    /**
     * Gauge: numar curent de produse active (actualizat de ProductService).
     */
    @Bean
    public Gauge activeProductsGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("store.products.active.count", activeProductsCount, AtomicInteger::get)
                .description("Current number of active products in the store")
                .tag("service", "store-service")
                .register(meterRegistry);
    }

    /**
     * Expunem AtomicInteger-ul pentru a fi actualizat de ProductService.
     */
    @Bean
    public AtomicInteger activeProductsGaugeValue() {
        return activeProductsCount;
    }

    /**
     * Timer: durata procesarii comenzilor.
     */
    @Bean
    public Timer orderProcessingTimer(MeterRegistry meterRegistry) {
        return Timer.builder("store.order.processing.duration")
                .description("Time taken to process an order")
                .tag("service", "store-service")
                .publishPercentiles(0.5, 0.95, 0.99) // p50, p95, p99
                .publishPercentileHistogram()
                .register(meterRegistry);
    }
}
