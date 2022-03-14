package com.github.yedp.ez.common.mq.rabbitmq.properties;

import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class RabbitMqProperty {
    private String host;
    private int port = 5672;
    private String virtualHost;
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration requestedHeartbeat;
    private Duration connectionTimeout;
    private String username;
    private String password;
    private String accessKeyId;
    private String accessKeySecret;
    private String securityToken;
    private String instanceId;
    private RabbitMqProducerProperty producer;
    private RabbitMqConsumerProperty consumer;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public Duration getRequestedHeartbeat() {
        return requestedHeartbeat;
    }

    public void setRequestedHeartbeat(Duration requestedHeartbeat) {
        this.requestedHeartbeat = requestedHeartbeat;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public RabbitMqProducerProperty getProducer() {
        return producer;
    }

    public void setProducer(RabbitMqProducerProperty producer) {
        this.producer = producer;
    }

    public RabbitMqConsumerProperty getConsumer() {
        return consumer;
    }

    public void setConsumer(RabbitMqConsumerProperty consumer) {
        this.consumer = consumer;
    }
}
