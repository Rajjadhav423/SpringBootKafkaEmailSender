package com.example.email.consumer.service;

import com.example.email.common.dto.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryScheduler {

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;
    private final TaskScheduler taskScheduler;

    @Value("${app.kafka.topics.email-retry:email-retry}")
    private String retryTopic;

    @Value("${app.kafka.topics.email-dlq:email-dlq}")
    private String dlqTopic;

    @Value("${app.retry.base-delay-seconds:5}")
    private long baseDelaySeconds;

    public void scheduleRetry(EmailEvent event, int attempt) {
        long delay = computeDelay(attempt);
        EmailEvent retryEvent = EmailEvent.builder()
                .id(event.getId())
                .to(event.getTo())
                .subject(event.getSubject())
                .body(event.getBody())
                .templateName(event.getTemplateName())
                .variables(event.getVariables())
                .scheduledAt(event.getScheduledAt())
                .retryCount(attempt)
                .correlationId(event.getCorrelationId())
                .build();

        Instant fireAt = Instant.now().plusSeconds(delay);
        taskScheduler.schedule(() -> {
            log.info("Publishing retry {} for email {}", attempt, event.getId());
            kafkaTemplate.send(retryTopic, retryEvent.getId().toString(), retryEvent);
        }, fireAt);
    }

    public void sendToDlq(EmailEvent event, Exception ex) {
        log.error("Sending email {} to DLQ after retries", event.getId(), ex);
        kafkaTemplate.send(dlqTopic, event.getId().toString(), event);
    }

    private long computeDelay(int attempt) {
        if (attempt <= 1) {
            return baseDelaySeconds;
        }
        if (attempt == 2) {
            return baseDelaySeconds * 3;
        }
        return baseDelaySeconds * 6;
    }
}
