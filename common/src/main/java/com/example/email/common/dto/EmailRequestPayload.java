package com.example.email.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class EmailRequestPayload {

    @Email
    @NotBlank
    private String to;

    @NotBlank
    @Size(max = 200)
    private String subject;

    @NotBlank
    private String body;

    private String template;

    private Map<String, String> variables = new HashMap<>();

    private LocalDateTime scheduledAt;
}
