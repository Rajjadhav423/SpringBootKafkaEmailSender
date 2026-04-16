package com.example.email.consumer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.email-requests:email-requests}")
    private String emailTopic;

    @Value("${app.kafka.topics.email-retry:email-retry}")
    private String retryTopic;

    @Value("${app.kafka.topics.email-dlq:email-dlq}")
    private String dlqTopic;

    @Bean
    public NewTopic emailRequestsTopic() {
        return TopicBuilder.name(emailTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic emailRetryTopic() {
        return TopicBuilder.name(retryTopic).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic emailDlqTopic() {
        return TopicBuilder.name(dlqTopic).partitions(1).replicas(1).build();
    }
}
