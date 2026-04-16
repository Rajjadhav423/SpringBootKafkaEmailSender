package com.emailsender.consumer.repository;

import com.emailsender.consumer.entity.EmailRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmailRequestRepository extends JpaRepository<EmailRequest, UUID> {
}
