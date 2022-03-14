package com.github.yedp.ez.common.mq.rabbitmq.actuate;


import java.util.Map;

import com.github.yedp.ez.common.mq.rabbitmq.DynamicRabbitMqClient;
import com.github.yedp.ez.common.mq.rabbitmq.config.RabbitMqAutoConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.actuate.autoconfigure.health.CompositeHealthContributorConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass({RabbitTemplate.class})
@AutoConfigureAfter({RabbitMqAutoConfiguration.class})
@ConditionalOnEnabledHealthIndicator("rabbit")
public class RabbitMqHealthContributorAutoConfiguration extends CompositeHealthContributorConfiguration<RabbitMqHealthIndicator, RabbitTemplate> {
    public RabbitMqHealthContributorAutoConfiguration() {
    }

    @Bean
    @ConditionalOnMissingBean(
        name = {"rabbitHealthIndicator", "rabbitHealthContributor"}
    )
    @ConditionalOnClass({DynamicRabbitMqClient.class})
    public HealthContributor rabbitHealthContributor(DynamicRabbitMqClient dynamicRabbitMqClient) {
        Map<String, RabbitTemplate> rabbitTemplates = dynamicRabbitMqClient.getAllRabbitTemplate();
        return rabbitTemplates != null && rabbitTemplates.size() != 0 ? (HealthContributor)this.createContributor(rabbitTemplates) : null;
    }
}
