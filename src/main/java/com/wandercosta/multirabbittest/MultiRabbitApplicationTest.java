package com.wandercosta.multirabbittest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactoryContextWrapper;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableRabbit
@SpringBootApplication
public class MultiRabbitApplicationTest {

    public static void main(final String... args) {
        SpringApplication.run(MultiRabbitApplicationTest.class, args);
    }

    @Component
    @EnableScheduling
    public static class SomeController {

        private static final String CONNECTION_PREFIX = "connectionName";
        private static final String EXCHANGE_NAME = "sampleExchange";
        private static final String ROUTING_KEY = "sampleRoutingKey";

        private final RabbitTemplate rabbitTemplate;
//        private final ConnectionFactoryContextWrapper contextWrapper;

        private static final int AMOUNT_OF_LISTENERS = 3;
        private volatile int defaultSemaphore = 0;
        private volatile int contextSemaphore = 0;

        SomeController(final RabbitTemplate rabbitTemplate
//                ,                       final ConnectionFactoryContextWrapper contextWrapper
        ) {
            this.rabbitTemplate = rabbitTemplate;
//            this.contextWrapper = contextWrapper;
        }

//        @Scheduled(fixedRate = 1_000L)
//        void sendMessageTheDefaultWay() {
//            this.defaultSemaphore = this.defaultSemaphore++ % AMOUNT_OF_LISTENERS;
//            final int id = defaultSemaphore;
//
//            if (id != 0) {
//                SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), CONNECTION_PREFIX + id);
//            }
//
//            final String exchange = EXCHANGE_NAME + id;
//            final String routingKey = ROUTING_KEY + id;
//            final String message = "message sent by the default method";
//
//            // Regular use of RabbitTemplate
//            rabbitTemplate.convertAndSend(exchange, routingKey, message);
//
//            if (id != 0) {
//                SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
//            }
//        }
//
//        @Scheduled(fixedRate = 1_000L)
//        void sendMessageUsingContextWrapper() {
//            this.contextSemaphore = this.contextSemaphore++ % AMOUNT_OF_LISTENERS;
//            final int id = contextSemaphore;
//
//            final String idWithPrefix = (id != 0) ? CONNECTION_PREFIX + id : null;
//
//            contextWrapper.run(idWithPrefix, () -> {
//                final String exchange = EXCHANGE_NAME + id;
//                final String routingKey = ROUTING_KEY + id;
//                final String message = "message sent with context wrapper";
//
//                // Regular use of RabbitTemplate
//                rabbitTemplate.convertAndSend(exchange, routingKey, message);
//            });
//        }
    }

    @Slf4j
    @Component
    public static class SomeListeners {

        private static final String SAMPLE_EXCHANGE = "sampleExchange";
        private static final String SAMPLE_ROUTING_KEY = "sampleRoutingKey";
        private static final String SAMPLE_QUEUE = "sampleQueue";

        private static final String SAMPLE_EXCHANGE_A = SAMPLE_EXCHANGE + "A";
        private static final String SAMPLE_ROUTING_KEY_A = SAMPLE_ROUTING_KEY + "A";
        private static final String SAMPLE_QUEUE_A = SAMPLE_QUEUE + "A";

        private static final String SAMPLE_EXCHANGE_B = SAMPLE_EXCHANGE + "B";
        private static final String SAMPLE_ROUTING_KEY_B = SAMPLE_ROUTING_KEY + "B";
        private static final String SAMPLE_QUEUE_B = SAMPLE_QUEUE + "B";

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue(SAMPLE_QUEUE),
                exchange = @Exchange(SAMPLE_EXCHANGE),
                key = SAMPLE_ROUTING_KEY))
        void listen(final String message) {
            log.info("Default Listener: {}", message);
        }

        @RabbitListener(containerFactory = "connectionNameA",
                bindings = @QueueBinding(
                value = @Queue(value = SAMPLE_QUEUE_A),
                exchange = @Exchange(value =SAMPLE_EXCHANGE_A),
                key = SAMPLE_ROUTING_KEY_A))
        void listenConnectionNameA(final String message) {
            log.info("Listener 'connectionNameA': {}", message);
        }

//        @RabbitListener(containerFactory = "connectionNameB", bindings = @QueueBinding(
//                value = @Queue(SAMPLE_QUEUE_B),
//                exchange = @Exchange(SAMPLE_EXCHANGE_B),
//                key = SAMPLE_ROUTING_KEY_B))
//        public void listenConnectionNameB(final String message) {
//            log.info("Listener 'connectionNameB': {}", message);
//        }
    }

}
