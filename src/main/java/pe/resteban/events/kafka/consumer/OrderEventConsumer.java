package pe.resteban.events.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import pe.resteban.events.model.OrderEvent;

@Slf4j
@Component
public class OrderEventConsumer {

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 3000)
    )
    @KafkaListener(topics = "${app.kafka.topics.orders}")
    public void consume(OrderEvent event) {
        log.info("Received OrderEvent: {}", event);
    }

    @DltHandler
    public void handleDlt(OrderEvent event) {
        log.error("OrderEvent reached DLT — discarded after all retry attempts: {}", event);
    }
}
