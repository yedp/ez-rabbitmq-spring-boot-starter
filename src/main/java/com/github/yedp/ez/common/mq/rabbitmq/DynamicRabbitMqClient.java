package com.github.yedp.ez.common.mq.rabbitmq;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class DynamicRabbitMqClient {
    private static final Logger log = LoggerFactory.getLogger(DynamicRabbitMqClient.class);
    private Map<String, RabbitTemplate> rabbitTemplateMap = new ConcurrentHashMap();

    public synchronized void addRabbitTemplate(String name, RabbitTemplate rabbitTemplate) {
        if (name != null && rabbitTemplate != null) {
            if (!this.rabbitTemplateMap.containsKey(name)) {
                this.rabbitTemplateMap.put(name, rabbitTemplate);
                log.info("DynamicRabbitMqClient - load a rabbitTemplate named [{}] success", name);
            } else {
                log.warn("DynamicRabbitMqClient - load a rabbitTemplate named [{}] failed, because it already exist", name);
            }
        }

    }

    public RabbitTemplate getRabbitTemplate(String name) {
        if (this.rabbitTemplateMap.containsKey(name)) {
            return this.rabbitTemplateMap.get(name);
        } else {
            throw new RuntimeException("could not find rabbitTemplate named [{}]" + name);
        }
    }

    public Map<String, RabbitTemplate> getAllRabbitTemplate() {
        return this.rabbitTemplateMap;
    }
}
