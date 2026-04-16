package com.example.email.common.repository;

import com.example.email.common.model.EmailRequest;
import com.example.email.common.model.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailRequestRepository extends JpaRepository<EmailRequest, UUID> {

    List<EmailRequest> findTop50ByStatusInAndScheduledAtLessThanEqualOrderByCreatedAt(Collection<EmailStatus> statuses, LocalDateTime scheduledBefore);

    Optional<EmailRequest> findByCorrelationId(String correlationId);
}
