package pe.resteban.events.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.resteban.events.kafka.producer.OrderEventProducer;
import pe.resteban.events.model.NotificationMessage;
import pe.resteban.events.model.OrderEvent;
import pe.resteban.events.rabbitmq.publisher.NotificationPublisher;
import pe.resteban.events.shared.response.ApiResponse;

@Tag(name = "Events", description = "Endpoints for triggering Kafka and RabbitMQ events manually")
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final OrderEventProducer orderEventProducer;
    private final NotificationPublisher notificationPublisher;

    @Operation(summary = "Publish an OrderEvent to Kafka")
    @PostMapping("/kafka")
    public ResponseEntity<ApiResponse<String>> sendKafkaEvent(@Valid @RequestBody OrderEvent event) {
        orderEventProducer.publish(event);
        return ResponseEntity.ok(ApiResponse.ok("OrderEvent published to Kafka topic 'orders-topic'"));
    }

    @Operation(summary = "Publish a NotificationMessage to RabbitMQ")
    @PostMapping("/rabbit")
    public ResponseEntity<ApiResponse<String>> sendRabbitEvent(@Valid @RequestBody NotificationMessage message) {
        notificationPublisher.publish(message);
        return ResponseEntity.ok(ApiResponse.ok("NotificationMessage published to RabbitMQ exchange 'notifications.exchange'"));
    }
}
