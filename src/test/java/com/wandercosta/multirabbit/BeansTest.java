package com.wandercosta.multirabbit;

import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_1;
import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_2;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_0;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_1;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_2;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_0;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_1;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils.RABBIT_ADMIN_BEAN_NAME;
import static org.springframework.amqp.rabbit.config.RabbitListenerConfigUtils.RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME;
import static org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration.MULTI_RABBIT_ADMIN_SUFFIX;
import static org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration.MULTI_RABBIT_CONTAINER_FACTORY_BEAN_NAME;

import com.wandercosta.multirabbit.TestConfigs.MultiRabbitDisabledConfig;
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
import org.springframework.amqp.rabbit.connection.RoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.autoconfigure.amqp.MultiRabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

class BeansTest {

    @Test
    @DisplayName("should not find MultiRabbit beans when it's disabled")
    void shouldEnsureNonMultiRabbitBeans() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, MultiRabbitDisabledConfig.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class);

        assertThat(ctx.getBean(RabbitAdmin.class)).isNotNull();
        assertThat(ctx.getBean(RabbitListenerContainerFactory.class)).isNotNull();
        assertThat(ctx.getBean(ConnectionFactory.class)).isNotInstanceOf(RoutingConnectionFactory.class);
        assertThat(ctx.getBean(RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME))
                .isNotInstanceOf(MultiRabbitListenerAnnotationBeanPostProcessor.class);

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should find MultiRabbit beans when it's enabled")
    void shouldEnsureMultiRabbitBeans() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, RabbitAutoConfiguration.class, MultiRabbitAutoConfiguration.class);

        assertThat(ctx.getBean(RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class)).isNotNull();
        assertThat(ctx.getBean(BROKER_NAME_1.concat(MULTI_RABBIT_ADMIN_SUFFIX), RabbitAdmin.class)).isNotNull();
        assertThat(ctx.getBean(BROKER_NAME_2.concat(MULTI_RABBIT_ADMIN_SUFFIX), RabbitAdmin.class)).isNotNull();

        assertThat(ctx.getBean(MULTI_RABBIT_CONTAINER_FACTORY_BEAN_NAME, RabbitListenerContainerFactory.class))
                .isNotNull();
        assertThat(ctx.getBean(BROKER_NAME_1, RabbitListenerContainerFactory.class)).isNotNull();
        assertThat(ctx.getBean(BROKER_NAME_2, RabbitListenerContainerFactory.class)).isNotNull();

        assertThat(ctx.getBean(ConnectionFactory.class)).isInstanceOf(RoutingConnectionFactory.class);

        assertThat(ctx.getBean(RABBIT_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME))
                .isInstanceOf(MultiRabbitListenerAnnotationBeanPostProcessor.class);

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should initialize listeners when MultiRabbit is enabled")
    void shouldInitializeListenersWhenEnabled() {
        final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, ThreeListenersBeans.class, RabbitAutoConfiguration.class,
                MultiRabbitAutoConfiguration.class);

        assertThat(ctx.getBeansOfType(org.springframework.amqp.core.Queue.class)).hasSize(3);
        assertThat(ctx.getBeansOfType(org.springframework.amqp.core.Binding.class)).hasSize(3);
        assertThat(ctx.getBeansOfType(org.springframework.amqp.core.DirectExchange.class)).hasSize(3);

        ctx.close(); // Close and stop the listeners
    }

    @Test
    @DisplayName("should fail to initialize listeners when MultiRabbit is disabled")
    void shouldFailToInitializeListenersWhenDisabled() {
        assertThrows(BeanCreationException.class, () -> new AnnotationConfigApplicationContext(
                ThreeBrokersConfig.class, MultiRabbitDisabledConfig.class, ThreeListenersBeans.class,
                RabbitAutoConfiguration.class, MultiRabbitAutoConfiguration.class));
    }

    @Component
    @EnableRabbit
    private static class ThreeListenersBeans {

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
