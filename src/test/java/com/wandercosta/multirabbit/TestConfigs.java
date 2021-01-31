package com.wandercosta.multirabbit;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

public class TestConfigs {

    /**
     * Configuration to provide 3 brokers.
     */
    @Configuration
    @PropertySource("classpath:application-three-brokers.properties")
    public static class ThreeBrokersConfig {
    }

    /**
     * Configuration to disabled MultiRabbit.
     */
    @Configuration
    @PropertySource("classpath:application-multi-rabbit-disabled.properties")
    public static class MultiRabbitDisabledConfig {
    }
}
