package pe.resteban.events.rabbitmq.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.resteban.events.model.NotificationMessage;
import pe.resteban.events.rabbitmq.config.RabbitMQConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(NotificationMessage message) {
        log.info("Publishing NotificationMessage to exchange '{}' with routing key '{}': {}",
                RabbitMQConfig.NOTIFICATIONS_EXCHANGE, RabbitMQConfig.ROUTING_KEY, message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATIONS_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}
