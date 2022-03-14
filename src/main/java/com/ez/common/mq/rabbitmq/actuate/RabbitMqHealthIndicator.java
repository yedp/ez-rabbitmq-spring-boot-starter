package com.ez.common.mq.rabbitmq.actuate;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.util.Assert;

public class RabbitMqHealthIndicator extends AbstractHealthIndicator {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqHealthIndicator(RabbitTemplate rabbitTemplate) {
        super("Rabbit health check failed");
        Assert.notNull(rabbitTemplate, "RabbitTemplate must not be null");
        this.rabbitTemplate = rabbitTemplate;
    }

    protected void doHealthCheck(Builder builder) throws Exception {
        builder.up().withDetail("version", this.getVersion());
    }

    private String getVersion() {
        return this.rabbitTemplate.execute((channel) -> {
            return channel.getConnection().getServerProperties().get("version").toString();
        });
    }
}