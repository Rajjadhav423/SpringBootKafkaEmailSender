package com.emailsender.consumer.service.impl;

import com.emailsender.common.enums.EmailStatus;
import com.emailsender.common.event.EmailEvent;
import com.emailsender.consumer.entity.EmailRequest;
import com.emailsender.consumer.repository.EmailRequestRepository;
import com.emailsender.consumer.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final EmailRequestRepository emailRequestRepository;

    @Override
    public void sendEmail(EmailEvent event) {
        log.info("Sending email to={}, correlationId={}", event.getTo(), event.getCorrelationId());

        if (event.getScheduledAt() != null && event.getScheduledAt().isAfter(LocalDateTime.now())) {
            log.info("Email id={} is scheduled for {}. Skipping for now.", event.getId(), event.getScheduledAt());
            updateEmailStatus(event, EmailStatus.SCHEDULED);
            return;
        }

        try {
            String body = resolveTemplate(event.getBody(), event.getVariables());
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(event.getTo());
            message.setSubject(event.getSubject());
            message.setText(body);
            mailSender.send(message);

            log.info("Email sent successfully to={}, id={}", event.getTo(), event.getId());
            updateEmailStatus(event, EmailStatus.SENT);
        } catch (Exception e) {
            log.error("Failed to send email to={}, id={}, error={}", event.getTo(), event.getId(), e.getMessage());
            throw e;
        }
    }

    private String resolveTemplate(String body, Map<String, String> variables) {
        if (body == null) return "";
        if (variables == null || variables.isEmpty()) return body;

        String resolved = body;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            resolved = resolved.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return resolved;
    }

    private void updateEmailStatus(EmailEvent event, EmailStatus status) {
        emailRequestRepository.findById(event.getId()).ifPresentOrElse(
                request -> {
                    request.setStatus(status);
                    request.setRetryCount(event.getRetryCount());
                    emailRequestRepository.save(request);
                },
                () -> {
                    EmailRequest request = EmailRequest.builder()
                            .id(event.getId())
                            .toEmail(event.getTo())
                            .subject(event.getSubject())
                            .body(event.getBody())
                            .templateName(event.getTemplate())
                            .status(status)
                            .retryCount(event.getRetryCount())
                            .correlationId(event.getCorrelationId())
                            .scheduledAt(event.getScheduledAt())
                            .build();
                    emailRequestRepository.save(request);
                }
        );
    }
}
