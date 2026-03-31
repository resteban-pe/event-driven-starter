package pe.resteban.events.rabbitmq.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.resteban.events.model.NotificationMessage;
import pe.resteban.events.rabbitmq.config.RabbitMQConfig;

@Slf4j
@Component
public class NotificationListener {

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATIONS_QUEUE)
    public void listen(NotificationMessage message) {
        log.info("Received NotificationMessage: {}", message);
    }
}
