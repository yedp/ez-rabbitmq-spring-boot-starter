package com.ez.common.mq.rabbitmq.properties;

import java.util.List;

public class RabbitMqConsumerProperty {
    private boolean enabled;
    private List<RabbitMqConsumerProperty.SubscribeConfig> subscribeList;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<SubscribeConfig> getSubscribeList() {
        return subscribeList;
    }

    public void setSubscribeList(List<SubscribeConfig> subscribeList) {
        this.subscribeList = subscribeList;
    }

    public static class SubscribeConfig {
        private String queue;
        private String messageListener;

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getMessageListener() {
            return messageListener;
        }

        public void setMessageListener(String messageListener) {
            this.messageListener = messageListener;
        }
    }
}
