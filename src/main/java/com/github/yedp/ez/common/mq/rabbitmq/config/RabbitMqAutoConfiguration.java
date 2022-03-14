package com.github.yedp.ez.common.mq.rabbitmq.config;

import com.github.yedp.ez.common.mq.rabbitmq.DynamicRabbitMqClient;
import com.github.yedp.ez.common.mq.rabbitmq.properties.RabbitMqConfigProperties;
import com.github.yedp.ez.common.mq.rabbitmq.properties.RabbitMqConsumerProperty;
import com.github.yedp.ez.common.mq.rabbitmq.properties.RabbitMqProducerProperty;
import com.github.yedp.ez.common.mq.rabbitmq.properties.RabbitMqProperty;
import com.rabbitmq.client.Channel;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.RabbitConnectionFactoryBean;
import org.springframework.amqp.rabbit.connection.SimplePropertyValueConnectionNameStrategy;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration(
    proxyBeanMethods = false
)
@ConditionalOnClass({RabbitTemplate.class, Channel.class})
@EnableConfigurationProperties({RabbitMqConfigProperties.class})
public class RabbitMqAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(RabbitMqAutoConfiguration.class);
    @Autowired
    private ApplicationContext applicationContext;


    @Bean
    @ConditionalOnMissingBean
    public ConnectionNameStrategy connectionNameStrategy() {
        return new SimplePropertyValueConnectionNameStrategy("spring.application.name");
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRabbitMqClient dynamicRabbitMqClient(RabbitMqConfigProperties properties, ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
        DynamicRabbitMqClient dynamicRabbitMqClient = new DynamicRabbitMqClient();
        AtomicInteger num = new AtomicInteger(0);
        Map<String, RabbitMqProperty> configs = properties.getConfigs();
        if (configs != null && !configs.isEmpty()) {
            MessageConverter messageConverter = new SimpleMessageConverter();
            Iterator<Entry<String, RabbitMqProperty>> configsIterator = configs.entrySet().iterator();

            while(configsIterator.hasNext()) {
                Entry<String, RabbitMqProperty> e = configsIterator.next();
                String name = e.getKey();
                RabbitMqProperty property = e.getValue();
                log.info("RabbitMq {} - Starting...", name);
                RabbitMqProducerProperty producerProperty = property.getProducer();
                RabbitMqConsumerProperty consumerProperty = property.getConsumer();
                boolean init = producerProperty != null && producerProperty.isEnabled() || consumerProperty != null && consumerProperty.isEnabled();
                if (!init) {
                    log.warn("RabbitMq {} - no need init", name);
                } else {
                    DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory)this.applicationContext.getAutowireCapableBeanFactory();
                    ConnectionFactory connectionFactory = this.createConnectionFactory(property, connectionNameStrategy);
                    if (producerProperty != null && producerProperty.isEnabled()) {
                        RabbitTemplate rabbitTemplate = this.createRabbitTemplate(name, connectionFactory, messageConverter, beanFactory, producerProperty);
                        dynamicRabbitMqClient.addRabbitTemplate(name, rabbitTemplate);
                        log.info("RabbitMq {} - producer RabbitTemplate init success.", name);
                    }

                    if (consumerProperty != null && consumerProperty.isEnabled()) {
                        this.createMessageListener(name, connectionFactory, messageConverter, beanFactory, consumerProperty);
                        log.info("RabbitMq {} - consumer init success.", name);
                    }

                    num.getAndIncrement();
                }
            }
        }

        log.info("dynamic-rabbitMqClient initial loaded [{}] RabbitMq", num.get());
        return dynamicRabbitMqClient;
    }

    private ConnectionFactory createConnectionFactory(RabbitMqProperty property, ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        property.getClass();
        map.from(property::getHost).whenNonNull().to(factory::setHost);
        property.getClass();
        map.from(property::getPort).to(factory::setPort);
        property.getClass();
        map.from(property::getVirtualHost).whenNonNull().to(factory::setVirtualHost);
        property.getClass();
        map.from(property::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds).to(factory::setRequestedHeartbeat);
        AliyunCredentialsProvider credentialsProvider = null;
        String username = property.getUsername();
        String password = property.getPassword();
        if (username != null && password != null) {
            map.from(username).whenNonNull().to(factory::setUsername);
            map.from(password).whenNonNull().to(factory::setPassword);
        } else {
            credentialsProvider = new AliyunCredentialsProvider(property.getAccessKeyId(), property.getAccessKeySecret(), property.getSecurityToken(), property.getInstanceId());
        }

        property.getClass();
        map.from(property::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis).to(factory::setConnectionTimeout);
        factory.afterPropertiesSet();
        com.rabbitmq.client.ConnectionFactory connectionFactory = factory.getObject();
        connectionFactory.setAutomaticRecoveryEnabled(true);
        log.error("rabbitmq connectionFactory setAutomaticRecoveryEnabled true");
        connectionFactory.setNetworkRecoveryInterval(5000);
        if (credentialsProvider != null) {
            connectionFactory.setCredentialsProvider(credentialsProvider);
        }

        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(connectionFactory);
        cachingConnectionFactory.setPublisherConfirmType(ConfirmType.CORRELATED);
        cachingConnectionFactory.setConnectionNameStrategy(connectionNameStrategy.getIfUnique());
        return cachingConnectionFactory;
    }

    private RabbitTemplate createRabbitTemplate(String name, ConnectionFactory connectionFactory, MessageConverter messageConverter, DefaultListableBeanFactory beanFactory, RabbitMqProducerProperty producerProperty) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RabbitTemplate.class);
        beanDefinitionBuilder.addConstructorArgValue(connectionFactory);
        beanDefinitionBuilder.addPropertyValue("messageConverter", messageConverter);
        beanDefinitionBuilder.addPropertyValue("exchange", producerProperty.getExchange());
        beanDefinitionBuilder.addPropertyValue("routingKey", producerProperty.getRoutingKey());
        AbstractBeanDefinition rawBeanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        String beanName = name + "-RabbitTemplate";
        beanFactory.registerBeanDefinition(beanName, rawBeanDefinition);
        RabbitTemplate rabbitTemplate = this.applicationContext.getBean(beanName, RabbitTemplate.class);
        if (!StringUtils.isEmpty(producerProperty.getConfirmCallback())) {
            ConfirmCallback confirmCallback = this.applicationContext.getBean(producerProperty.getConfirmCallback(), ConfirmCallback.class);
            rabbitTemplate.setConfirmCallback(confirmCallback);
        }

        return rabbitTemplate;
    }

    private void createMessageListener(String name, ConnectionFactory connectionFactory, MessageConverter messageConverter, DefaultListableBeanFactory beanFactory, RabbitMqConsumerProperty consumerProperty) {
        List<RabbitMqConsumerProperty.SubscribeConfig> subscribeList = consumerProperty.getSubscribeList();
        if (subscribeList != null && !subscribeList.isEmpty()) {
            Map<String, List<RabbitMqConsumerProperty.SubscribeConfig>> subscribeConfigMap = new HashMap();
            Iterator var8 = subscribeList.iterator();

            String messageListenerBeanName;
            while(var8.hasNext()) {
                RabbitMqConsumerProperty.SubscribeConfig subscribeConfig = (RabbitMqConsumerProperty.SubscribeConfig)var8.next();
                if (!StringUtils.isEmpty(subscribeConfig.getQueue()) && !StringUtils.isEmpty(subscribeConfig.getMessageListener())) {
                    messageListenerBeanName = subscribeConfig.getMessageListener();
                    if (subscribeConfigMap.containsKey(messageListenerBeanName)) {
                        (subscribeConfigMap.get(messageListenerBeanName)).add(subscribeConfig);
                    } else {
                        List<RabbitMqConsumerProperty.SubscribeConfig> list = new ArrayList();
                        list.add(subscribeConfig);
                        subscribeConfigMap.put(messageListenerBeanName, list);
                    }
                }
            }

            var8 = subscribeConfigMap.entrySet().iterator();

            while(true) {
                MessageListener messageListener;
                List subscribeConfigList;
                do {
                    if (!var8.hasNext()) {
                        return;
                    }

                    Entry<String, List<RabbitMqConsumerProperty.SubscribeConfig>> entry = (Entry)var8.next();
                    messageListenerBeanName = entry.getKey();
                    subscribeConfigList = entry.getValue();
                    messageListener = this.applicationContext.getBean(messageListenerBeanName, MessageListener.class);
                } while(messageListener == null);

                BeanDefinitionBuilder mlaBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MessageListenerAdapter.class);
                mlaBeanDefinitionBuilder.addConstructorArgValue(messageListener);
                mlaBeanDefinitionBuilder.addConstructorArgValue(messageConverter);
                AbstractBeanDefinition rawBeanDefinition = mlaBeanDefinitionBuilder.getRawBeanDefinition();
                String beanName = name + "-" + messageListenerBeanName + "-MessageListenerAdapter";
                beanFactory.registerBeanDefinition(beanName, rawBeanDefinition);
                MessageListenerAdapter messageListenerAdapter = this.applicationContext.getBean(beanName, MessageListenerAdapter.class);
                BeanDefinitionBuilder mlcBeanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(SimpleMessageListenerContainer.class);
                mlcBeanDefinitionBuilder.addPropertyValue("connectionFactory", connectionFactory);
                mlcBeanDefinitionBuilder.addPropertyValue("messageListener", messageListenerAdapter);
                AbstractBeanDefinition mlcRawBeanDefinition = mlcBeanDefinitionBuilder.getRawBeanDefinition();
                String mlcBeanName = name + "-" + messageListenerBeanName + "-MessageListenerContainer";
                beanFactory.registerBeanDefinition(mlcBeanName, mlcRawBeanDefinition);
                List<Queue> queues = new ArrayList(subscribeConfigList.size());
                Iterator var21 = subscribeConfigList.iterator();

                while(var21.hasNext()) {
                    RabbitMqConsumerProperty.SubscribeConfig subscribeConfig = (RabbitMqConsumerProperty.SubscribeConfig)var21.next();
                    queues.add(new Queue(subscribeConfig.getQueue(), true, false, false));
                }

                SimpleMessageListenerContainer messageListenerContainer = this.applicationContext.getBean(mlcBeanName, SimpleMessageListenerContainer.class);
                messageListenerContainer.setQueues(queues.toArray(new Queue[queues.size()]));
                messageListenerContainer.setAcknowledgeMode(AcknowledgeMode.MANUAL);
                messageListenerContainer.setAutoStartup(true);
            }
        }
    }
}
