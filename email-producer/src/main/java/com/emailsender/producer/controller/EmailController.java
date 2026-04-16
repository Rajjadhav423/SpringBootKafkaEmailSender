package com.emailsender.producer.controller;

import com.emailsender.common.dto.EmailRequestDto;
import com.emailsender.common.dto.EmailStatusDto;
import com.emailsender.producer.service.EmailProducerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
@Tag(name = "Email API", description = "API for sending and tracking emails")
public class EmailController {

    private final EmailProducerService emailProducerService;

    @PostMapping("/send")
    @Operation(summary = "Send an email", description = "Submits an email request to be sent asynchronously via Kafka")
    public ResponseEntity<EmailStatusDto> sendEmail(@Valid @RequestBody EmailRequestDto request) {
        log.info("Received email send request for: {}", request.getTo());
        EmailStatusDto response = emailProducerService.sendEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get email status", description = "Returns the current status of an email request")
    public ResponseEntity<EmailStatusDto> getEmailStatus(@PathVariable UUID id) {
        log.info("Fetching email status for id: {}", id);
        EmailStatusDto response = emailProducerService.getEmailStatus(id);
        return ResponseEntity.ok(response);
    }
}
