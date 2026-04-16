package com.example.email.consumer.listener;

import com.example.email.common.dto.EmailEvent;
import com.example.email.consumer.service.EmailProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailKafkaListener {

    private final EmailProcessorService processorService;

    @KafkaListener(
            topics = {"${app.kafka.topics.email-requests:email-requests}", "${app.kafka.topics.email-retry:email-retry}"},
            groupId = "${app.kafka.consumer-group:email-consumer-group}")
    public void consume(@Payload EmailEvent event) {
        log.info("Consumed email event {}", event.getId());
        processorService.process(event);
    }
}
