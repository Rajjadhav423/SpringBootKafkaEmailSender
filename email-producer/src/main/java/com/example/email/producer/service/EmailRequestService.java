package com.example.email.producer.service;

import com.example.email.common.dto.EmailEvent;
import com.example.email.common.dto.EmailRequestPayload;
import com.example.email.common.dto.EmailStatusResponse;
import com.example.email.common.model.EmailRequest;
import com.example.email.common.model.EmailStatus;
import com.example.email.common.repository.EmailRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailRequestService {

    private final EmailRequestRepository repository;
    private final KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @Value("${app.kafka.topics.email-requests:email-requests}")
    private String emailTopic;

    public UUID acceptRequest(EmailRequestPayload payload) {
        LocalDateTime scheduledTime = payload.getScheduledAt() != null ? payload.getScheduledAt() : LocalDateTime.now();
        EmailRequest request = EmailRequest.builder()
                .toEmail(payload.getTo())
                .subject(payload.getSubject())
                .body(payload.getBody())
                .templateName(payload.getTemplate())
                .variables(payload.getVariables())
                .scheduledAt(scheduledTime)
                .status(determineInitialStatus(payload))
                .build();

        if (request.getVariables() == null) {
            request.setVariables(new java.util.HashMap<>());
        }

        EmailRequest saved = repository.save(request);
        log.info("Accepted email request {} for {}", saved.getId(), saved.getToEmail());

        if (saved.getStatus() == EmailStatus.PENDING || saved.getStatus() == EmailStatus.QUEUED) {
            publish(saved);
        }
        return saved.getId();
    }

    public EmailStatusResponse getStatus(UUID id) {
        EmailRequest request = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Email not found: " + id));
        return new EmailStatusResponse(request.getId(), request.getStatus(), request.getRetryCount(), request.getScheduledAt(), request.getLastError());
    }

    @Scheduled(fixedDelayString = "${app.scheduler.poll-interval-ms:5000}")
    @Transactional
    public void dispatchScheduled() {
        List<EmailStatus> statuses = Arrays.asList(EmailStatus.PENDING, EmailStatus.SCHEDULED);
        List<EmailRequest> due = repository.findTop50ByStatusInAndScheduledAtLessThanEqualOrderByCreatedAt(statuses, LocalDateTime.now());
        for (EmailRequest request : due) {
            publish(request);
        }
    }

    private void publish(EmailRequest request) {
        if (request.getStatus() == EmailStatus.SENT || request.getStatus() == EmailStatus.FAILED || request.getStatus() == EmailStatus.DLQ) {
            return;
        }
        EmailEvent event = EmailEvent.builder()
                .id(request.getId())
                .to(request.getToEmail())
                .subject(request.getSubject())
                .body(request.getBody())
                .templateName(request.getTemplateName())
                .variables(request.getVariables())
                .scheduledAt(request.getScheduledAt())
                .retryCount(request.getRetryCount())
                .correlationId(request.getCorrelationId())
                .build();

        request.setStatus(EmailStatus.QUEUED);
        repository.save(request);
        kafkaTemplate.send(emailTopic, event.getId().toString(), event);
        log.info("Published email {} to Kafka topic {}", request.getId(), emailTopic);
    }

    private EmailStatus determineInitialStatus(EmailRequestPayload payload) {
        if (payload.getScheduledAt() != null && payload.getScheduledAt().isAfter(LocalDateTime.now())) {
            return EmailStatus.SCHEDULED;
        }
        return EmailStatus.PENDING;
    }
}
