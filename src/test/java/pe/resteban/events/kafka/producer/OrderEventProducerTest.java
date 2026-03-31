package pe.resteban.events.kafka.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import pe.resteban.events.model.OrderEvent;

import java.time.Instant;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    OrderEventProducer producer;

    @BeforeEach
    void setUp() {
        // @Value no se inyecta con Mockito — se establece manualmente
        ReflectionTestUtils.setField(producer, "ordersTopic", "orders-topic");
    }

    @Test
    void publish_shouldCallKafkaTemplateSendExactlyOnce() {
        OrderEvent event = new OrderEvent("ORD-001", "CREATED", Instant.parse("2026-03-30T17:00:00Z"));

        producer.publish(event);

        verify(kafkaTemplate, times(1)).send("orders-topic", "ORD-001", event);
    }

    @Test
    void publish_shouldUseOrderIdAsKey() {
        OrderEvent event = new OrderEvent("ORD-999", "SHIPPED", Instant.now());

        producer.publish(event);

        verify(kafkaTemplate).send("orders-topic", "ORD-999", event);
    }
}
