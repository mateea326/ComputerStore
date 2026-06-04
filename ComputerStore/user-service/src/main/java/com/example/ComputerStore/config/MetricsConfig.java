package com.example.ComputerStore.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configurație metrici custom pentru user-service.
 * Toate metricile sunt automat expuse la /actuator/prometheus.
 */
@Configuration
public class MetricsConfig {

    // Gauge: utilizatori activi (logați în sesiunile curente)
    private final AtomicInteger activeUsersCount = new AtomicInteger(0);

    /**
     * Counter: autentificări reușite.
     */
    @Bean
    public Counter userLoginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.success.total")
                .description("Total number of successful user logins")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Counter: autentificări eșuate (credențiale greșite).
     */
    @Bean
    public Counter userLoginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.login.failure.total")
                .description("Total number of failed login attempts")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Counter: înregistrări noi de utilizatori.
     */
    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("user.registrations.total")
                .description("Total number of new user registrations")
                .tag("service", "user-service")
                .register(meterRegistry);
    }

    /**
     * Gauge: utilizatori activi în memorie.
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
     * Timer: durata autentificării.
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
