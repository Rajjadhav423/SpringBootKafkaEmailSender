package com.example.email.producer.controller;

import com.example.email.common.dto.EmailRequestPayload;
import com.example.email.common.dto.EmailStatusResponse;
import com.example.email.producer.service.EmailRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailRequestService emailRequestService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendEmail(@Valid @RequestBody EmailRequestPayload payload) {
        UUID id = emailRequestService.acceptRequest(payload);
        return ResponseEntity.accepted().body(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmailStatusResponse> status(@PathVariable UUID id) {
        return ResponseEntity.ok(emailRequestService.getStatus(id));
    }
}
