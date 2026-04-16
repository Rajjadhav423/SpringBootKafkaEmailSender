package com.example.email.producer.service;

import com.example.email.common.dto.EmailRequestPayload;
import com.example.email.common.model.EmailRequest;
import com.example.email.common.repository.EmailRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailRequestServiceTest {

    @Mock
    private EmailRequestRepository repository;

    @Mock
    private KafkaTemplate<String, ?> kafkaTemplate;

    @InjectMocks
    private EmailRequestService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "emailTopic", "email-requests");
    }

    @Test
    void acceptRequestPublishesImmediatelyWhenNotScheduled() {
        when(repository.save(any(EmailRequest.class))).thenAnswer(invocation -> {
            EmailRequest req = invocation.getArgument(0);
            if (req.getId() == null) {
                req.setId(UUID.randomUUID());
            }
            return req;
        });

        EmailRequestPayload payload = new EmailRequestPayload();
        payload.setTo("user@example.com");
        payload.setSubject("Hello");
        payload.setBody("Test body");

        UUID id = service.acceptRequest(payload);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());
        assertThat(keyCaptor.getValue()).isEqualTo(id.toString());
    }
}
