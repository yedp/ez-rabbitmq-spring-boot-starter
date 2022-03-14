package com.ez.common.mq.rabbitmq.properties;

public class RabbitMqProducerProperty {
    private boolean enabled;
    private String exchange;
    private String routingKey;
    private String confirmCallback;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getConfirmCallback() {
        return confirmCallback;
    }

    public void setConfirmCallback(String confirmCallback) {
        this.confirmCallback = confirmCallback;
    }
}
