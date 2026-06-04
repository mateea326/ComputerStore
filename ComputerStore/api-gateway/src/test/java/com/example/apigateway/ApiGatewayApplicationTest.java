package com.example.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.cloud.gateway.routes=",
        "management.zipkin.tracing.endpoint=http://localhost:9411/api/v2/spans"
})
class ApiGatewayApplicationTest {

    @Test
    void contextLoads() {
        // Verificăm că contextul Spring se încarcă corect
    }
}
