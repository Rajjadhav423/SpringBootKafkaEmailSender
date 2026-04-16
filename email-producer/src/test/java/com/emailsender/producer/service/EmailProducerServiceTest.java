package com.emailsender.producer.service;

import com.emailsender.common.dto.EmailRequestDto;
import com.emailsender.common.dto.EmailStatusDto;
import com.emailsender.common.enums.EmailStatus;
import com.emailsender.common.event.EmailEvent;
import com.emailsender.producer.entity.EmailRequest;
import com.emailsender.producer.repository.EmailRequestRepository;
import com.emailsender.producer.service.impl.EmailProducerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailProducerServiceTest {

    @Mock
    private KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @Mock
    private EmailRequestRepository emailRequestRepository;

    @InjectMocks
    private EmailProducerServiceImpl emailProducerService;

    private EmailRequest savedRequest;

    @BeforeEach
    void setUp() {
        savedRequest = EmailRequest.builder()
                .id(UUID.randomUUID())
                .toEmail("test@example.com")
                .subject("Test Subject")
                .body("Hello World")
                .status(EmailStatus.PENDING)
                .retryCount(0)
                .build();
    }

    @Test
    void sendEmail_shouldSaveAndPublishEvent() {
        // Arrange
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .body("Hello {{name}}")
                .variables(Map.of("name", "John"))
                .build();

        when(emailRequestRepository.save(any(EmailRequest.class))).thenReturn(savedRequest);

        // Act
        EmailStatusDto result = emailProducerService.sendEmail(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(EmailStatus.PENDING);
        verify(emailRequestRepository, times(1)).save(any(EmailRequest.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(EmailEvent.class));
    }

    @Test
    void sendEmail_shouldSetScheduledStatus_whenScheduledAtIsProvided() {
        // Arrange
        EmailRequestDto request = EmailRequestDto.builder()
                .to("test@example.com")
                .subject("Scheduled Email")
                .body("Scheduled body")
                .scheduledAt(java.time.LocalDateTime.now().plusDays(1))
                .build();

        EmailRequest scheduledRequest = EmailRequest.builder()
                .id(UUID.randomUUID())
                .toEmail("test@example.com")
                .subject("Scheduled Email")
                .status(EmailStatus.SCHEDULED)
                .retryCount(0)
                .build();

        when(emailRequestRepository.save(any(EmailRequest.class))).thenReturn(scheduledRequest);

        // Act
        EmailStatusDto result = emailProducerService.sendEmail(request);

        // Assert
        assertThat(result.getStatus()).isEqualTo(EmailStatus.SCHEDULED);
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(EmailEvent.class));
    }

    @Test
    void getEmailStatus_shouldReturnStatus_whenIdExists() {
        // Arrange
        UUID id = savedRequest.getId();
        when(emailRequestRepository.findById(id)).thenReturn(Optional.of(savedRequest));

        // Act
        EmailStatusDto result = emailProducerService.getEmailStatus(id);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getTo()).isEqualTo("test@example.com");
    }

    @Test
    void getEmailStatus_shouldThrowException_whenIdNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(emailRequestRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> emailProducerService.getEmailStatus(id))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");
    }
}
