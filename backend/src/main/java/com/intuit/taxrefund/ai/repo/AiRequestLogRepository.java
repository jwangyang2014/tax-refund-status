package com.intuit.taxrefund.ai.repo;

import com.intuit.taxrefund.ai.model.AiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AiRequestLogRepository extends JpaRepository {
    Optional<AiRequestLog> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
