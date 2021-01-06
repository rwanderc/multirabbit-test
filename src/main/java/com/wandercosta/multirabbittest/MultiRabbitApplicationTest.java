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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableRabbit(multi = true)
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
        private final ConnectionFactoryContextWrapper contextWrapper;

        SomeController(final RabbitTemplate rabbitTemplate,
                       final ConnectionFactoryContextWrapper contextWrapper) {
            this.rabbitTemplate = rabbitTemplate;
            this.contextWrapper = contextWrapper;
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageTheDefaultWay() {
            sendDefaultWay(0);
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageToConnection1TheDefaultWay() {
            sendDefaultWay(1);
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageToConnection2TheDefaultWay() {
            sendDefaultWay(2);
        }

        private void sendDefaultWay(int id) {
            if (id != 0) {
                SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), CONNECTION_PREFIX + id);
            }

            final String exchange = EXCHANGE_NAME + id;
            final String routingKey = ROUTING_KEY + id;
            final String message = "message sent by default way: " + id;

            // Regular use of RabbitTemplate
            rabbitTemplate.convertAndSend(exchange, routingKey, message);

            if (id != 0) {
                SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
            }
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageWithContextWrapper() {
            sendWithContextWrapper(0);
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageToConnection1WithContextWrapper() {
            sendWithContextWrapper(1);
        }

        @Scheduled(fixedDelay = 5_000L)
        void sendMessageToConnection2WithContextWrapper() {
            sendWithContextWrapper(2);
        }

        private void sendWithContextWrapper(int id) {
            contextWrapper.run(CONNECTION_PREFIX + id, () -> {
                final String exchange = EXCHANGE_NAME + id;
                final String routingKey = ROUTING_KEY + id;
                final String message = "message sent by context wrapper: " + id;

                // Regular use of RabbitTemplate
                rabbitTemplate.convertAndSend(exchange, routingKey, message);
            });
        }
    }

    @Slf4j
    @Component
    public static class SomeListeners {

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue("sampleQueue0"),
                exchange = @Exchange("sampleExchange0"),
                key = "sampleRoutingKey0"))
        void listen(final String message) {
            log.info("Default Listener: {}", message);
        }

        @RabbitListener(containerFactory = "connectionName1",
                bindings = @QueueBinding(
                        value = @Queue(value = "sampleQueue1"),
                        exchange = @Exchange(value = "sampleExchange1"),
                        key = "sampleRoutingKey1"))
        void listenConnectionNameA(final String message) {
            log.info("Listener 'connectionNameA': {}", message);
        }

        @RabbitListener(containerFactory = "connectionName2", bindings = @QueueBinding(
                value = @Queue("sampleQueue2"),
                exchange = @Exchange("sampleExchange2"),
                key = "sampleRoutingKey2"))
        public void listenConnectionNameB(final String message) {
            log.info("Listener 'connectionNameB': {}", message);
        }
    }

}
