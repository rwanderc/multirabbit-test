package com.wandercosta.multirabbit;

import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_1;
import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_2;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_0;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_1;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_2;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_0;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_1;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_2;

import com.wandercosta.multirabbit.TestConfigs.ThreeBrokersConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.MultiRabbitListenerAnnotationBeanPostProcessor;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * {@link MultiRabbitAutoConfiguration} is normally triggered before the processing of the Listeners by the
 * {@link MultiRabbitListenerAnnotationBeanPostProcessor}. However, this does not happen whenever there is no injection
 * of {@link ConnectionFactory}.
 * This test makes sure to test MultiRabbit without the injection of a RabbitTemplate as a workaround for the
 * initialization.
 */
class AutoConfigInitializationBugIntegrationTest {

    @Test
    @DisplayName("Should start MultiRabbit AutoConfig without reference to ConnectionFactory")
    void shouldStartContextWithoutRabbitTemplate() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, RabbitAutoConfiguration.class, MultiRabbitAutoConfiguration.class,
                AutoConfigInitializationBugIntegrationTest.ListenerBeans.class);
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
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_1),
                value = @Queue(),
                key = ROUTING_KEY_1))
        void listenBroker1(final String message) {
        }

        @RabbitListener(containerFactory = BROKER_NAME_2, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_2),
                value = @Queue(),
                key = ROUTING_KEY_2))
        void listenBroker2(final String message) {
        }
    }
}
