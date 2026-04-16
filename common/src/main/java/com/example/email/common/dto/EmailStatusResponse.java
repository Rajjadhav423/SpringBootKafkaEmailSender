package com.example.email.common.dto;

import com.example.email.common.model.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class EmailStatusResponse {
    private UUID id;
    private EmailStatus status;
    private int retryCount;
    private LocalDateTime scheduledAt;
    private String lastError;
}
