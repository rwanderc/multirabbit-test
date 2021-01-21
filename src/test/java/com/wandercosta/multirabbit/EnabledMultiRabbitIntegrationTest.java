package com.wandercosta.multirabbit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactoryContextWrapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
        EnabledMultiRabbitIntegrationTest.Application.class,
        Memory.class})
@ActiveProfiles("three-brokers")
public class EnabledMultiRabbitIntegrationTest {

    @Autowired
    private ConnectionFactoryContextWrapper contextWrapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Memory memory;

    @BeforeEach
    void beforeEach() {
        memory.clear();
    }

    @Test
    @DisplayName("should send to and listen from the default broker")
    void shouldListenToDefault() {
        final String message = RandomString.make();
        contextWrapper.run(() -> rabbitTemplate.convertAndSend(Application.EXCHANGE_0, Application.RK_0, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, memory.get("default")));
    }

    @Test
    @DisplayName("should send to and listen from the broker connectionName1")
    void shouldListenToConnection1() {
        final String message = RandomString.make();
        contextWrapper.run("connectionName1",
                () -> rabbitTemplate.convertAndSend(Application.EXCHANGE_1, Application.RK_1, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, memory.get(Application.BROKER_NAME_1)));
    }

    @Test
    @DisplayName("should send to and listen from the broker connectionName2")
    void shouldListenToConnection2() {
        final String message = RandomString.make();
        contextWrapper.run("connectionName2",
                () -> rabbitTemplate.convertAndSend(Application.EXCHANGE_2, Application.RK_2, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, memory.get(Application.BROKER_NAME_2)));

    }

    @EnableRabbit(multi = true)
    @SpringBootApplication
    public static class Application {

        public static void main(final String... args) {
            SpringApplication.run(EnabledMultiRabbitIntegrationTest.class, args);
        }

        public static final String EXCHANGE_0 = "sampleExchange0";
        public static final String QUEUE_0 = "sampleQueue0";
        public static final String RK_0 = "sampleRoutingKey0";

        public static final String BROKER_NAME_1 = "connectionName1";
        public static final String EXCHANGE_1 = "sampleExchange1";
        public static final String QUEUE_1 = "sampleQueue1";
        public static final String RK_1 = "sampleRoutingKey1";

        public static final String BROKER_NAME_2 = "connectionName2";
        public static final String EXCHANGE_2 = "sampleExchange2";
        public static final String QUEUE_2 = "sampleQueue2";
        public static final String RK_2 = "sampleRoutingKey2";

        @Autowired
        private Memory memory;

        @Autowired
        private RabbitTemplate rabbitTemplate; // TODO This must not be necessary

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_0),
                value = @Queue(QUEUE_0),
                key = RK_0))
        void listen(final String message) {
            memory.put("default", message);
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_1),
                value = @Queue(QUEUE_1),
                key = RK_1))
        void listenConnectionName1(final String message) {
            memory.put(BROKER_NAME_1, message);
        }

        @RabbitListener(containerFactory = BROKER_NAME_2, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_2),
                value = @Queue(QUEUE_2),
                key = RK_2))
        void listenConnectionName2(final String message) {
            memory.put(BROKER_NAME_2, message);
        }
    }
}
