package com.emailsender.consumer.service;

import com.emailsender.common.event.EmailEvent;

public interface EmailSenderService {
    void sendEmail(EmailEvent event);
}
