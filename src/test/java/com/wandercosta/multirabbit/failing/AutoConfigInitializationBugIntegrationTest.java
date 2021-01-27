package com.wandercosta.multirabbit.failing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.MultiRabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@link MultiRabbitAutoConfiguration} is normally triggered before the processing of the Listeners by the
 * {@link MultiRabbitListenerAnnotationBeanPostProcessor}. However, this does not happen whenever there is no injection
 * of {@link ConnectionFactory}.
 * This test makes sure to test MultiRabbit without the injection of a RabbitTemplate as a workaround for the
 * initialization.
 */
@SpringBootTest(classes = AutoConfigInitializationBugIntegrationTest.Application.class)
@ActiveProfiles("three-brokers")
public class AutoConfigInitializationBugIntegrationTest {

    @Test
    @DisplayName("Should start MultiRabbit AutoConfig without reference to ConnectionFactory")
    void shouldStartContextWithoutRabbitTemplate() {
    }

    @EnableRabbit
    @SpringBootApplication
    public static class Application {

        public static void main(final String... args) {
            SpringApplication.run(AutoConfigInitializationBugIntegrationTest.class, args);
        }

        public static final String EXCHANGE_0 = "sampleExchange0";
        public static final String RK_0 = "sampleRoutingKey0";

        public static final String BROKER_NAME_1 = "connectionName1";
        public static final String EXCHANGE_1 = "sampleExchange1";
        public static final String RK_1 = "sampleRoutingKey1";

        @RabbitListener(admin = "amqpAdmin", bindings = @QueueBinding(
//        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_0),
                value = @Queue(exclusive = "true", durable = "false", autoDelete = "true"),
                key = RK_0))
        void listen(final String message) {
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, admin = BROKER_NAME_1 + "-admin", bindings = @QueueBinding(
//        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_1),
                value = @Queue(exclusive = "true", durable = "false", autoDelete = "true"),
                key = RK_1))
        void listenConnectionName1(final String message) {
        }
    }
}
