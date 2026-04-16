package com.emailsender.consumer.listener;

import com.emailsender.common.event.EmailEvent;
import com.emailsender.consumer.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumerListener {

    private static final String DLQ_TOPIC = "email-dlq";

    private final EmailSenderService emailSenderService;
    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 3.0, maxDelay = 30000),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = "-dlq",
            autoCreateTopics = "true"
    )
    @KafkaListener(topics = "email-requests", groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeEmailRequest(ConsumerRecord<String, EmailEvent> record, Acknowledgment acknowledgment) {
        EmailEvent event = record.value();
        log.info("Received email event: id={}, correlationId={}, to={}, attempt={}",
                event.getId(), event.getCorrelationId(), event.getTo(), event.getRetryCount() + 1);

        try {
            emailSenderService.sendEmail(event);
            acknowledgment.acknowledge();
            log.info("Email processed successfully: id={}", event.getId());
        } catch (Exception e) {
            log.error("Failed to process email event: id={}, error={}", event.getId(), e.getMessage());
            throw e;
        }
    }

    @KafkaListener(topics = DLQ_TOPIC, groupId = "${spring.kafka.consumer.group-id}-dlq",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeDeadLetter(ConsumerRecord<String, EmailEvent> record, Acknowledgment acknowledgment) {
        EmailEvent event = record.value();
        log.error("Email sent to DLQ: id={}, to={}, retryCount={}",
                event.getId(), event.getTo(), event.getRetryCount());
        acknowledgment.acknowledge();
    }
}
