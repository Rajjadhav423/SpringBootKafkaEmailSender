package com.emailsender.producer.repository;

import com.emailsender.common.enums.EmailStatus;
import com.emailsender.producer.entity.EmailRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailRequestRepository extends JpaRepository<EmailRequest, UUID> {
    List<EmailRequest> findByStatus(EmailStatus status);
    Optional<EmailRequest> findByCorrelationId(String correlationId);
}
