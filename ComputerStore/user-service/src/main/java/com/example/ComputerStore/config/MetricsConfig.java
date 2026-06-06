package com.example.ComputerStore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuratie metrici custom pentru user-service.
 * Toate metricile sunt automat expuse la /actuator/prometheus.
 */
@Configuration
public class MetricsConfig {

    // Gauge: utilizatori activi (logati in sesiunile curente)
    private final AtomicInteger activeUsersCount = new AtomicInteger(0);

    /**
     * Counter: autentificari reusite.
     */
    @Bean
    public Counter userLoginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.success.total")
                .description("Total number of successful user logins")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Counter: autentificari esuate (credentiale gresite).
     */
    @Bean
    public Counter userLoginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.failure.total")
                .description("Total number of failed login attempts")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Counter: inregistrari noi de utilizatori.
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.registrations.total")
                .description("Total number of new user registrations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Gauge: utilizatori activi in memorie.
     */
    @Bean
    public Gauge activeUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("user.active.count", activeUsersCount, AtomicInteger::get)
                .description("Current number of active/logged-in users")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Expunem AtomicInteger-ul pentru a fi actualizat de UserService.
     */
    @Bean
    public AtomicInteger activeUsersGaugeValue() {
        return activeUsersCount;
    }

    /**
     * Timer: durata autentificarii.
     */
    @Bean
    public Timer userAuthenticationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("user.authentication.duration")
                .description("Time taken to authenticate a user")
                .tag("service", "user-service")
                .publishPercentiles(0.5, 0.95, 0.99) // p50, p95, p99
                .publishPercentileHistogram()
                .register(meterRegistry);
    }
}
