package com.intuit.taxrefund.refund.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "refund_status_event",
    indexes = {
        @Index(name = "ix_rse_user_year_time", columnList = "user_id,tax_year,occurred_at"),
        @Index(name = "ix_rse_year_time", columnList = "tax_year,occurred_at")
    }
)
public class RefundStatusEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "user_id")
    private Long userId;

    @Column(nullable = false, name = "tax_year")
    private int taxYear;

    @Column(name = "filing_state", length = 2)
    private String filingState;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private RefundStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "to_status")
    private RefundStatus toStatus;

    @Column(name = "expected_amount", precision = 18, scale = 2)
    private BigDecimal expectedAmount;

    @Column(name = "irs_tracking_id", length = 100)
    private String irsTrackingId;

    @Column(nullable = false, length = 40)
    private String source; // IRS|SIMULATION|BACKFILL

    @Column(nullable = false, name = "occurred_at")
    private Instant occurredAt = Instant.now();

    protected RefundStatusEvent() {}

    private RefundStatusEvent(
        Long userId,
        int taxYear,
        String filingState,
        RefundStatus fromStatus,
        RefundStatus toStatus,
        BigDecimal expectedAmount,
        String irsTrackingId,
        String source
    ) {
        this.userId = userId;
        this.taxYear = taxYear;
        this.filingState = filingState;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.expectedAmount = expectedAmount;
        this.irsTrackingId = irsTrackingId;
        this.source = source;
        this.occurredAt = Instant.now();
    }

    public static RefundStatusEvent of(
        Long userId,
        int taxYear,
        String filingState,
        RefundStatus fromStatus,
        RefundStatus toStatus,
        BigDecimal expectedAmount,
        String irsTrackingId,
        String source
    ) {
        return new RefundStatusEvent(userId, taxYear, filingState, fromStatus, toStatus, expectedAmount, irsTrackingId, source);
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public int getTaxYear() { return taxYear; }
    public String getFilingState() { return filingState; }
    public RefundStatus getFromStatus() { return fromStatus; }
    public RefundStatus getToStatus() { return toStatus; }
    public BigDecimal getExpectedAmount() { return expectedAmount; }
    public String getIrsTrackingId() { return irsTrackingId; }
    public String getSource() { return source; }
    public Instant getOccurredAt() { return occurredAt; }
}