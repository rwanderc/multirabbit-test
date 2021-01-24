package com.wandercosta.multirabbittest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class MultiRabbitApplicationTest {

    public static void main(final String... args) {
        SpringApplication.run(MultiRabbitApplicationTest.class, args);
    }

    @EnableRabbit
    @Component
    public static class SomeListeners {

        private static final Logger LOGGER = LoggerFactory.getLogger(SomeListeners.class);

        @RabbitListener(bindings = @QueueBinding(
                value = @Queue("sampleQueue0"),
                exchange = @Exchange("sampleExchange0"),
                key = "sampleRoutingKey0"))
        void listen(final String message) {
            LOGGER.info("Default Listener: {}", message);
        }

        @RabbitListener(containerFactory = "connectionName1",
                bindings = @QueueBinding(
                        value = @Queue(value = "sampleQueue1"),
                        exchange = @Exchange(value = "sampleExchange1"),
                        key = "sampleRoutingKey1"))
        void listenConnectionNameA(final String message) {
            LOGGER.info("Listener 'connectionName1': {}", message);
        }

        @RabbitListener(containerFactory = "connectionName2", bindings = @QueueBinding(
                value = @Queue("sampleQueue2"),
                exchange = @Exchange("sampleExchange2"),
                key = "sampleRoutingKey2"))
        public void listenConnectionNameB(final String message) {
            LOGGER.info("Listener 'connectionName2': {}", message);
        }
    }
}
