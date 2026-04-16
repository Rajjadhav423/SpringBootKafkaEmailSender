package com.emailsender.producer.service.impl;

import com.emailsender.common.dto.EmailRequestDto;
import com.emailsender.common.dto.EmailStatusDto;
import com.emailsender.common.enums.EmailStatus;
import com.emailsender.common.event.EmailEvent;
import com.emailsender.producer.entity.EmailRequest;
import com.emailsender.producer.repository.EmailRequestRepository;
import com.emailsender.producer.service.EmailProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProducerServiceImpl implements EmailProducerService {

    private static final String EMAIL_REQUESTS_TOPIC = "email-requests";

    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;
    private final EmailRequestRepository emailRequestRepository;

    @Override
    public EmailStatusDto sendEmail(EmailRequestDto request) {
        String correlationId = UUID.randomUUID().toString();

        EmailRequest emailRequest = EmailRequest.builder()
                .toEmail(request.getTo())
                .subject(request.getSubject())
                .body(request.getBody())
                .templateName(request.getTemplate())
                .status(request.getScheduledAt() != null ? EmailStatus.SCHEDULED : EmailStatus.PENDING)
                .correlationId(correlationId)
                .scheduledAt(request.getScheduledAt())
                .build();

        emailRequest = emailRequestRepository.save(emailRequest);
        log.info("Email request saved with id={}, correlationId={}", emailRequest.getId(), correlationId);

        EmailEvent event = EmailEvent.builder()
                .id(emailRequest.getId())
                .correlationId(correlationId)
                .to(request.getTo())
                .subject(request.getSubject())
                .body(request.getBody())
                .template(request.getTemplate())
                .variables(request.getVariables())
                .scheduledAt(request.getScheduledAt())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(EMAIL_REQUESTS_TOPIC, correlationId, event);
        log.info("Email event published to topic={}, id={}", EMAIL_REQUESTS_TOPIC, emailRequest.getId());

        return toStatusDto(emailRequest);
    }

    @Override
    public EmailStatusDto getEmailStatus(UUID id) {
        EmailRequest request = emailRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Email request not found with id: " + id));
        return toStatusDto(request);
    }

    private EmailStatusDto toStatusDto(EmailRequest request) {
        return EmailStatusDto.builder()
                .id(request.getId())
                .to(request.getToEmail())
                .subject(request.getSubject())
                .status(request.getStatus())
                .retryCount(request.getRetryCount())
                .createdAt(request.getCreatedAt())
                .updatedAt(request.getUpdatedAt())
                .scheduledAt(request.getScheduledAt())
                .build();
    }
}
