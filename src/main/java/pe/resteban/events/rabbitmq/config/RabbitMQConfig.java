package pe.resteban.events.rabbitmq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String NOTIFICATIONS_QUEUE    = "notifications.queue";
    public static final String NOTIFICATIONS_DLQ      = "notifications.dlq";
    public static final String NOTIFICATIONS_EXCHANGE = "notifications.exchange";
    public static final String ROUTING_KEY            = "notification";

    // ── Queues ────────────────────────────────────────────────────────────────

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(NOTIFICATIONS_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue notificationsDlq() {
        return QueueBuilder.durable(NOTIFICATIONS_DLQ).build();
    }

    // ── Exchange & Binding ────────────────────────────────────────────────────

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(NOTIFICATIONS_EXCHANGE);
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(notificationsQueue)
                .to(notificationsExchange)
                .with(ROUTING_KEY);
    }

    // ── Serialization ─────────────────────────────────────────────────────────

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
