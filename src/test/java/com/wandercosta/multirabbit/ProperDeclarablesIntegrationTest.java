package com.wandercosta.multirabbit;

import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_1;
import static com.wandercosta.multirabbit.TestConstants.BROKER_NAME_2;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_0;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_1;
import static com.wandercosta.multirabbit.TestConstants.EXCHANGE_2;
import static com.wandercosta.multirabbit.TestConstants.QUEUE_0;
import static com.wandercosta.multirabbit.TestConstants.QUEUE_1;
import static com.wandercosta.multirabbit.TestConstants.QUEUE_2;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_0;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_1;
import static com.wandercosta.multirabbit.TestConstants.ROUTING_KEY_2;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.amqp.rabbit.annotation.MultiRabbitListenerAnnotationBeanPostProcessor.RABBIT_ADMIN_SUFFIX;
import static org.springframework.amqp.rabbit.annotation.RabbitListenerAnnotationBeanPostProcessor.DEFAULT_RABBIT_ADMIN_BEAN_NAME;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
        ProperDeclarablesIntegrationTest.Application.class,
        Memory.class})
@ActiveProfiles("three-brokers")
public class ProperDeclarablesIntegrationTest {

    @Autowired
    private BeanFactory beanFactory;

    @Test
    @DisplayName("should find proper declarable in broker0 and no other")
    void shouldFindProperDeclarableInBroker0AndNoOther() {
        final RabbitAdmin admin = beanFactory.getBean(DEFAULT_RABBIT_ADMIN_BEAN_NAME, RabbitAdmin.class);
        assertThat(admin.getQueueInfo(QUEUE_0).getName()).isEqualTo(QUEUE_0);
        assertThat(admin.getQueueInfo(QUEUE_1)).isNull();
        assertThat(admin.getQueueInfo(QUEUE_2)).isNull();
    }

    @Test
    @DisplayName("should find proper declarable in broker1 and no other")
    void shouldFindProperDeclarableInBroker1AndNoOther() {
        final RabbitAdmin admin = beanFactory.getBean(BROKER_NAME_1 + RABBIT_ADMIN_SUFFIX, RabbitAdmin.class);
        assertThat(admin.getQueueInfo(QUEUE_0)).isNull();
        assertThat(admin.getQueueInfo(QUEUE_1).getName()).isEqualTo(QUEUE_1);
        assertThat(admin.getQueueInfo(QUEUE_2)).isNull();
    }

    @Test
    @DisplayName("should find proper declarable in broker2 and no other")
    void shouldFindProperDeclarableInBroker2AndNoOther() {
        final RabbitAdmin admin = beanFactory.getBean(BROKER_NAME_2 + RABBIT_ADMIN_SUFFIX, RabbitAdmin.class);
        assertThat(admin.getQueueInfo(QUEUE_0)).isNull();
        assertThat(admin.getQueueInfo(QUEUE_1)).isNull();
        assertThat(admin.getQueueInfo(QUEUE_2).getName()).isEqualTo(QUEUE_2);
    }

    @EnableRabbit
    @SpringBootApplication
    public static class Application {

        @RabbitListener(bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_0),
                value = @Queue(name = QUEUE_0, exclusive = "true", durable = "false", autoDelete = "true"),
                key = ROUTING_KEY_0))
        void listenBroker0() {
        }

        @RabbitListener(containerFactory = BROKER_NAME_1, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_1),
                value = @Queue(name = QUEUE_1, exclusive = "true", durable = "false", autoDelete = "true"),
                key = ROUTING_KEY_1))
        void listenBroken1() {
        }

        @RabbitListener(containerFactory = BROKER_NAME_2, bindings = @QueueBinding(
                exchange = @Exchange(EXCHANGE_2),
                value = @Queue(name = QUEUE_2, exclusive = "true", durable = "false", autoDelete = "true"),
                key = ROUTING_KEY_2))
        void listenBroken2() {
        }
    }
}
