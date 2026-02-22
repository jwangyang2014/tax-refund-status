package com.intuit.taxrefund.refund.repo;

import com.intuit.taxrefund.refund.model.RefundStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundStatusEventRepository extends JpaRepository<RefundStatusEvent, Long> {
}