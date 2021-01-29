package com.wandercosta.multirabbit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class TestConfigs {

    @Configuration
    @PropertySource("classpath:application-three-brokers.properties")
    public static class ThreeBrokersTestConfig {
    }

    @Configuration
    @PropertySource("classpath:application-multi-disabled.properties")
    public static class DisabledMultiRabbitConfig {
    }
}
