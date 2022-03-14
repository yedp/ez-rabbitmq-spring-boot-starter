package com.ez.common.mq.rabbitmq.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
    prefix = "rabbitmq"
)
public class RabbitMqConfigProperties {
    private Map<String, RabbitMqProperty> configs = new LinkedHashMap();

    public Map<String, RabbitMqProperty> getConfigs() {
        return configs;
    }

    public void setConfigs(Map<String, RabbitMqProperty> configs) {
        this.configs = configs;
    }
}
