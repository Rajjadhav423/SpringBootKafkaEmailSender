package com.emailsender.producer.service;

import com.emailsender.common.dto.EmailRequestDto;
import com.emailsender.common.dto.EmailStatusDto;

import java.util.UUID;

public interface EmailProducerService {
    EmailStatusDto sendEmail(EmailRequestDto request);
    EmailStatusDto getEmailStatus(UUID id);
}
