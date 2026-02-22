package com.intuit.taxrefund.ai.repo;

import com.intuit.taxrefund.ai.model.RefundEtaPrediction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundEtaPredictionRepository extends JpaRepository<RefundEtaPrediction, Long> {

    Optional<RefundEtaPrediction> findTopByUserIdAndTaxYearAndStatusOrderByCreatedAtDesc(
        Long userId, int taxYear, String status
    );
    boolean existsByUserIdAndTaxYearAndStatusAndModelVersion(Long userId, int taxYear, String status, String modelVersion);
}