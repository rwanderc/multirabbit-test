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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wandercosta.multirabbit.TestConfigs.MultiRabbitDisabledConfig;
import com.wandercosta.multirabbit.TestConfigs.ThreeBrokersConfig;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
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

public class DisabledMultiRabbitEnd2EndIntegrationTest {

    private static final Memory MEMORY = new Memory();

    @BeforeEach
    void beforeEach() {
        MEMORY.clear();
    }

    @Test
    @DisplayName("should send to and listen from the default broker")
    void shouldListenToDefault() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, MultiRabbitDisabledConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, SingleListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run(() -> rabbitTemplate.convertAndSend(EXCHANGE_0, ROUTING_KEY_0, message));
        await().timeout(Duration.FIVE_SECONDS).untilAsserted(() -> assertEquals(message, MEMORY.get("default")));

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should send event to " + BROKER_NAME_1 + " and never listen back")
    void shouldListenToBroker1() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, MultiRabbitDisabledConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, SingleListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run(BROKER_NAME_1, () -> rabbitTemplate.convertAndSend(EXCHANGE_1, ROUTING_KEY_1, message));
        assertThrows(ConditionTimeoutException.class, () -> await()
                .timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, MEMORY.get("default"))));

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should send to " + BROKER_NAME_2 + " and never listen back")
    void shouldListenToConnection2() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, MultiRabbitDisabledConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class, SingleListenerBeans.class);

        final ConnectionFactoryContextWrapper contextWrapper = ctx.getBean(ConnectionFactoryContextWrapper.class);
        final RabbitTemplate rabbitTemplate = ctx.getBean(RabbitTemplate.class);

        final String message = RandomString.make();
        contextWrapper.run(BROKER_NAME_2, () -> rabbitTemplate.convertAndSend(EXCHANGE_2, ROUTING_KEY_2, message));
        assertThrows(ConditionTimeoutException.class, () -> await()
                .timeout(Duration.FIVE_SECONDS)
                .untilAsserted(() -> assertEquals(message, MEMORY.get("default"))));

        ctx.close(); // Close and stop the listeners
    }

    @Component
    @EnableRabbit
    private static class SingleListenerBeans {

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_0),
                value = @Queue(),
                key = ROUTING_KEY_0))
        void listen(final String message) {
            MEMORY.put("default", message);
        }
    }
}
