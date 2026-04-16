package com.emailsender.consumer.service;

import com.emailsender.common.enums.EmailStatus;
import com.emailsender.common.event.EmailEvent;
import com.emailsender.consumer.entity.EmailRequest;
import com.emailsender.consumer.repository.EmailRequestRepository;
import com.emailsender.consumer.service.impl.EmailSenderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private EmailRequestRepository emailRequestRepository;

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    @Test
    void sendEmail_shouldSendEmailAndUpdateStatus() {
        // Arrange
        UUID id = UUID.randomUUID();
        EmailEvent event = EmailEvent.builder()
                .id(id)
                .correlationId("corr-123")
                .to("test@example.com")
                .subject("Test Subject")
                .body("Hello {{name}}")
                .variables(Map.of("name", "John"))
                .retryCount(0)
                .build();

        EmailRequest existingRequest = EmailRequest.builder()
                .id(id)
                .toEmail("test@example.com")
                .subject("Test Subject")
                .status(EmailStatus.PENDING)
                .build();

        when(emailRequestRepository.findById(id)).thenReturn(Optional.of(existingRequest));
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        // Act
        emailSenderService.sendEmail(event);

        // Assert
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        ArgumentCaptor<EmailRequest> captor = ArgumentCaptor.forClass(EmailRequest.class);
        verify(emailRequestRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(EmailStatus.SENT);
    }

    @Test
    void sendEmail_shouldResolveTemplateVariables() {
        // Arrange
        UUID id = UUID.randomUUID();
        EmailEvent event = EmailEvent.builder()
                .id(id)
                .to("test@example.com")
                .subject("Welcome!")
                .body("Hello {{name}}, welcome to {{company}}!")
                .variables(Map.of("name", "John", "company", "Acme"))
                .retryCount(0)
                .build();

        when(emailRequestRepository.findById(id)).thenReturn(Optional.empty());
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        // Act
        emailSenderService.sendEmail(event);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Hello John, welcome to Acme!");
    }

    @Test
    void sendEmail_shouldSkipSending_whenScheduledInFuture() {
        // Arrange
        UUID id = UUID.randomUUID();
        EmailEvent event = EmailEvent.builder()
                .id(id)
                .to("test@example.com")
                .subject("Future Email")
                .body("Scheduled")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .retryCount(0)
                .build();

        when(emailRequestRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        emailSenderService.sendEmail(event);

        // Assert
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_shouldThrowException_whenMailSenderFails() {
        // Arrange
        UUID id = UUID.randomUUID();
        EmailEvent event = EmailEvent.builder()
                .id(id)
                .to("test@example.com")
                .subject("Test")
                .body("Body")
                .retryCount(0)
                .build();

        doThrow(new RuntimeException("SMTP failure")).when(mailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        assertThatThrownBy(() -> emailSenderService.sendEmail(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP failure");
    }
}
