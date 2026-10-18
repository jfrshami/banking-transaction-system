package com.jfrshami.banking;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankingApplicationTests {

    @Test
    void applicationStartsAndHealthEndpointIsUp() throws Exception {
        // Start the real web application without SpringExtension (which requires JUnit 6).
        // Persistence is excluded only in this smoke test; no database or Docker is needed.
        try (ConfigurableApplicationContext context = SpringApplication.run(BankingApplication.class,
                "--server.port=0",
                "--spring.autoconfigure.exclude="
                        + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration");
                HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build()) {
            int port = context.getEnvironment().getRequiredProperty("local.server.port", Integer.class);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/actuator/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            Map<?, ?> health = context.getBean(ObjectMapper.class).readValue(response.body(), Map.class);
            assertEquals("UP", health.get("status"));
        }
    }
}
