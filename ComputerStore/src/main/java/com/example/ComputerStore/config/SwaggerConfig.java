package com.example.ComputerStore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI computerStoreOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8081");
        devServer.setDescription("Development Server");

        Contact contact = new Contact();
        contact.setName("Computer Store Support");
        contact.setEmail("support@computerstore.com");

        License license = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Computer Store API")
                .version("1.0.0")
                .contact(contact)
                .description("REST API for PC Components E-commerce Store. " +
                        "This API provides endpoints for managing customers, products, orders, and shopping cart functionality.")
                .license(license);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
}