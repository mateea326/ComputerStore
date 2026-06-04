package com.example.ComputerStore.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) throws Exception {
        log.info("DataInitializer running: Database user initialization is handled by user-service.");
    }
}
