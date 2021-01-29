package com.wandercosta.multirabbit;

import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_1;
import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_2;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_0;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_1;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_2;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_0;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_1;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_2;
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
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

class EnabledMultiRabbitIntegrationTest {

    private static final Memory MEMORY = new Memory();

    @BeforeEach
    void beforeEach() {
        MEMORY.clear();
    }

    @Test
    @DisplayName("should send to and listen from the default broker")
    void shouldListenToDefault() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                TestConfigs.ThreeBrokersTestConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, ListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run(() -> rabbitTemplate.convertAndSend(EXCHANGE_0, ROUTING_KEY_0, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, MEMORY.get("default")));

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should send to and listen from the broker connectionName1")
    void shouldListenToConnection1() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                TestConfigs.ThreeBrokersTestConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, ListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run("connectionName1",
                () -> rabbitTemplate.convertAndSend(EXCHANGE_1, ROUTING_KEY_1, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, MEMORY.get(BROKER_NAME_1)));

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should send to and listen from the broker connectionName2")
    void shouldListenToConnection2() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                TestConfigs.ThreeBrokersTestConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, ListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run("connectionName2",
                () -> rabbitTemplate.convertAndSend(EXCHANGE_2, ROUTING_KEY_2, message));
        await().timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, MEMORY.get(BROKER_NAME_2)));

        ctx.close(); // Close and stop the listeners
    }

    @Component
    @EnableRabbit
    private static class ListenerBeans {

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_0),
                value = @Queue(),
                key = ROUTING_KEY_0))
        void listenBroker0(final String message) {
            MEMORY.put("default", message);
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_1),
                value = @Queue(),
                key = ROUTING_KEY_1))
        void listenBroker1(final String message) {
            MEMORY.put(BROKER_NAME_1, message);
        }

        @RabbitListener(containerFactory = BROKER_NAME_2, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_2),
                value = @Queue(),
                key = ROUTING_KEY_2))
        void listenBroker2(final String message) {
            MEMORY.put(BROKER_NAME_2, message);
        }
    }
}
