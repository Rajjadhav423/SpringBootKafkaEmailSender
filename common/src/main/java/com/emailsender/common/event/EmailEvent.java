package com.emailsender.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailEvent {
    private UUID id;
    private String correlationId;
    private String to;
    private String subject;
    private String body;
    private String template;
    private Map<String, String> variables;
    private LocalDateTime scheduledAt;
    private int retryCount;
    private LocalDateTime createdAt;
}
