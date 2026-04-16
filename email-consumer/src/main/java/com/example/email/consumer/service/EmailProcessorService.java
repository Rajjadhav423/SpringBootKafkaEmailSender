package com.example.email.consumer.service;

import com.example.email.common.dto.EmailEvent;
import com.example.email.common.model.EmailRequest;
import com.example.email.common.model.EmailStatus;
import com.example.email.common.repository.EmailRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessorService {

    private final EmailRequestRepository repository;
    private final EmailSenderService emailSenderService;
    private final RetryScheduler retryScheduler;

    @Value("${app.retry.max-attempts:3}")
    private int maxAttempts;

    @Transactional
    public void process(EmailEvent event) {
        EmailRequest request = findOrCreate(event);

        if (request.getStatus() == EmailStatus.SENT || request.getStatus() == EmailStatus.DLQ) {
            log.info("Skipping email {} with terminal status {}", request.getId(), request.getStatus());
            return;
        }

        try {
            request.setStatus(EmailStatus.PENDING);
            repository.save(request);

            emailSenderService.sendEmail(event);

            request.setStatus(EmailStatus.SENT);
            request.setLastError(null);
            repository.save(request);
            log.info("Email {} sent successfully to {}", request.getId(), request.getToEmail());
        } catch (Exception ex) {
            handleFailure(event, request, ex);
        }
    }

    private void handleFailure(EmailEvent event, EmailRequest request, Exception ex) {
        int attempt = request.getRetryCount() + 1;
        request.setRetryCount(attempt);
        request.setLastError(ex.getMessage());

        if (attempt >= maxAttempts) {
            request.setStatus(EmailStatus.DLQ);
            repository.save(request);
            retryScheduler.sendToDlq(event, ex);
            log.error("Email {} moved to DLQ after {} attempts", request.getId(), attempt, ex);
        } else {
            request.setStatus(EmailStatus.FAILED);
            repository.save(request);
            retryScheduler.scheduleRetry(event, attempt);
            log.warn("Email {} failed on attempt {}. Scheduling retry.", request.getId(), attempt, ex);
        }
    }

    private EmailRequest findOrCreate(EmailEvent event) {
        Optional<EmailRequest> existing = repository.findById(event.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        EmailRequest request = EmailRequest.builder()
                .id(event.getId() != null ? event.getId() : UUID.randomUUID())
                .toEmail(event.getTo())
                .subject(event.getSubject())
                .body(event.getBody())
                .templateName(event.getTemplateName())
                .variables(event.getVariables())
                .scheduledAt(event.getScheduledAt() != null ? event.getScheduledAt() : LocalDateTime.now())
                .status(EmailStatus.PENDING)
                .retryCount(event.getRetryCount())
                .correlationId(event.getCorrelationId())
                .build();
        return repository.save(request);
    }
}
