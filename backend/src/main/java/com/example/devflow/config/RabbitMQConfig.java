package com.example.devflow.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for the activity log messaging pipeline.
 * <p>
 * Declares a durable queue, a direct exchange, and a binding between them.
 * Also configures a Jackson2JsonMessageConverter so messages are serialized
 * to/from JSON, and a RabbitTemplate that uses that converter.
 * <p>
 * NOTE: Acknowledge mode is left at Spring AMQP's default (AUTO) — manual
 * ack, retry, and DLQ are intentionally deferred to a later week.
 */
@Configuration
public class RabbitMQConfig {

    public static final String ACTIVITY_LOG_QUEUE = "activity-log-queue";
    public static final String ACTIVITY_LOG_EXCHANGE = "activity-log-exchange";
    public static final String ACTIVITY_LOG_ROUTING_KEY = "activity.log";

    /**
     * Durable queue that holds activity log messages until a consumer picks them up.
     */
    @Bean
    public Queue activityLogQueue() {
        return new Queue(ACTIVITY_LOG_QUEUE, true);
    }

    /**
     * Direct exchange that routes activity log messages to the queue.
     */
    @Bean
    public DirectExchange activityLogExchange() {
        return new DirectExchange(ACTIVITY_LOG_EXCHANGE);
    }

    /**
     * Binds the queue to the exchange with the routing key "activity.log".
     */
    @Bean
    public Binding activityLogBinding(Queue activityLogQueue, DirectExchange activityLogExchange) {
        return BindingBuilder.bind(activityLogQueue)
                .to(activityLogExchange)
                .with(ACTIVITY_LOG_ROUTING_KEY);
    }

    /**
     * JSON message converter so ActivityLogMessage is serialized as JSON.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configured to use the JSON message converter.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    /**
     * RabbitAdmin that declares the queue/exchange/binding topology on the broker.
     * <p>
     * Because there is no consumer yet (deferred to a later week), the topology
     * would otherwise not be auto-declared. We explicitly initialize it at
     * startup so the Producer can publish into an existing queue.
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    /**
     * Forces the RabbitAdmin to declare the queue/exchange/binding at startup,
     * so the Producer can publish into an existing queue without a consumer.
     */
    @Bean
    public ApplicationRunner rabbitTopologyInitializer(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }
}
