package com.example.ComputerStore.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Health Indicator custom pentru baza de date a store-service.
 * Apare la /actuator/health ca "storeDatabase".
 */
@Component("storeDatabase")
public class StoreDatabaseHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(StoreDatabaseHealthIndicator.class);

    private final JdbcTemplate jdbcTemplate;

    public StoreDatabaseHealthIndicator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public Health health() {
        try {
            // Verificam conexiunea cu o query simpla
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            if (result != null && result == 1) {
                // Obtinem cateva statistici utile
                Integer productCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM computer_store.product", Integer.class
                );

                return Health.up()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("schema", "computer_store")
                        .withDetail("status", "Connected")
                        .withDetail("productsCount", productCount != null ? productCount : 0)
                        .withDetail("query", "SELECT 1 -> OK")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "PostgreSQL")
                        .withDetail("error", "Unexpected query result")
                        .build();
            }
        } catch (Exception e) {
            log.error("[HEALTH] Store database health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("error", e.getMessage())
                    .withDetail("status", "Disconnected")
                    .build();
        }
    }
}
