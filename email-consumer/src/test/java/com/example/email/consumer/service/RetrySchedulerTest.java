package com.example.email.consumer.service;

import com.example.email.common.dto.EmailEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RetrySchedulerTest {

    @Mock
    private KafkaTemplate<String, EmailEvent> kafkaTemplate;

    @Mock
    private TaskScheduler taskScheduler;

    @InjectMocks
    private RetryScheduler retryScheduler;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(retryScheduler, "retryTopic", "email-retry");
        ReflectionTestUtils.setField(retryScheduler, "dlqTopic", "email-dlq");
        ReflectionTestUtils.setField(retryScheduler, "baseDelaySeconds", 1L);
    }

    @Test
    void scheduleRetryRunsAndPublishes() {
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        when(taskScheduler.schedule(runnableCaptor.capture(), any(Instant.class))).thenReturn(null);

        EmailEvent event = EmailEvent.builder()
                .id(UUID.randomUUID())
                .to("user@example.com")
                .subject("Hello")
                .body("Body")
                .build();

        retryScheduler.scheduleRetry(event, 1);

        Runnable scheduled = runnableCaptor.getValue();
        scheduled.run();

        verify(kafkaTemplate).send(eq("email-retry"), eq(event.getId().toString()), any(EmailEvent.class));
    }
}
