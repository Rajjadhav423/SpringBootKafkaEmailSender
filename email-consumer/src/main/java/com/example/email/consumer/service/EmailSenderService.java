package com.example.email.consumer.service;

import com.example.email.common.dto.EmailEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendEmail(EmailEvent event) throws MessagingException {
        String html = renderBody(event);
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
        helper.setTo(event.getTo());
        helper.setSubject(event.getSubject());
        helper.setText(html, true);
        mailSender.send(mimeMessage);
        log.info("Email [{}] sent to {}", event.getId(), event.getTo());
    }

    private String renderBody(EmailEvent event) {
        if (event.getTemplateName() != null && !event.getTemplateName().isBlank()) {
            Context context = new Context();
            context.setVariables(new java.util.HashMap<>(event.getVariables()));
            return templateEngine.process(event.getTemplateName(), context);
        }
        return event.getBody();
    }
}
