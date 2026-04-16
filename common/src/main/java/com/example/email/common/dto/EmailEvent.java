package com.example.email.common.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class EmailEvent {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String to;
    private String subject;
    private String body;
    private String templateName;
    @Builder.Default
    private Map<String, String> variables = new HashMap<>();
    private LocalDateTime scheduledAt;
    @Builder.Default
    private int retryCount = 0;
    private String correlationId;
}
