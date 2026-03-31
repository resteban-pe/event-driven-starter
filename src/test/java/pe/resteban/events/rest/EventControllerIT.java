package pe.resteban.events.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventControllerIT {

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
    );

    @Container
    static final RabbitMQContainer rabbitmq = new RabbitMQContainer(
            DockerImageName.parse("rabbitmq:3.13-management-alpine")
    );

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.rabbitmq.username", rabbitmq::getAdminUsername);
        registry.add("spring.rabbitmq.password", rabbitmq::getAdminPassword);
    }

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void postKafkaEvent_shouldReturn2xx() {
        String body = """
                {"orderId": "ORD-001", "status": "CREATED", "timestamp": "2026-03-30T17:00:00Z"}
                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/events/kafka", jsonEntity(body), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void postRabbitEvent_shouldReturn2xx() {
        String body = """
                {"id": "MSG-001", "type": "EMAIL", "payload": "test payload"}
                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/events/rabbit", jsonEntity(body), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    private HttpEntity<String> jsonEntity(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
