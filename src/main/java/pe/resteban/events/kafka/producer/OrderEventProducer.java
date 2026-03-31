package pe.resteban.events.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import pe.resteban.events.model.OrderEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Value("${app.kafka.topics.orders}")
    private String ordersTopic;

    public void publish(OrderEvent event) {
        log.info("Publishing OrderEvent to topic '{}': {}", ordersTopic, event);
        kafkaTemplate.send(ordersTopic, event.orderId(), event);
    }
}
